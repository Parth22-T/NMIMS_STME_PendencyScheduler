/**
 * NMIMS STME Dynamic Academic Pendency Scheduler
 * Central Multi-Device Real-Time Synchronization Server
 * 
 * Zero external dependencies (Pure Node.js built-ins: http, fs, os, url, path).
 * Run: node server.js
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const url = require('url');

const PORT = process.env.PORT || 8080;
const DB_FILE = path.join(__dirname, 'server_state.json');

// In-memory synchronized state database
let db = {
    serverStartTime: Date.now(),
    requests: [],
    timetableSlots: [],
    facultyLeaves: [],
    subjectUpdates: []
};

// Load existing state if available
if (fs.existsSync(DB_FILE)) {
    try {
        const raw = fs.readFileSync(DB_FILE, 'utf8');
        db = Object.assign(db, JSON.parse(raw));
        console.log(`[DB] Loaded ${db.requests.length} requests, ${db.timetableSlots.length} slots, ${db.facultyLeaves.length} leaves from disk.`);
    } catch (e) {
        console.warn('[DB] Warning loading saved state, starting fresh:', e.message);
    }
}

function saveDb() {
    try {
        fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2), 'utf8');
    } catch (e) {
        console.error('[DB] Error persisting state to disk:', e.message);
    }
}

function getLocalIpAddresses() {
    const interfaces = os.networkInterfaces();
    const ips = [];
    for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                ips.push(iface.address);
            }
        }
    }
    return ips;
}

function parseJsonBody(req, callback) {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
        try {
            const data = body ? JSON.parse(body) : {};
            callback(null, data);
        } catch (e) {
            callback(e, null);
        }
    });
}

function sendJson(res, statusCode, data) {
    res.writeHead(statusCode, {
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type'
    });
    res.end(JSON.stringify(data));
}

const server = http.createServer((req, res) => {
    // Enable CORS for web/local clients
    if (req.method === 'OPTIONS') {
        res.writeHead(204, {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type'
        });
        res.end();
        return;
    }

    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;

    // 1. Health & Server Status
    if (pathname === '/api/status' && req.method === 'GET') {
        sendJson(res, 200, {
            status: 'ONLINE',
            service: 'NMIMS STME Academic Pendency Sync Server',
            academicYear: 'AY 2026-27',
            serverTime: Date.now(),
            uptimeSeconds: Math.floor((Date.now() - db.serverStartTime) / 1000),
            totalRequests: db.requests.length,
            totalRescheduledSlots: db.timetableSlots.length,
            totalLeaves: db.facultyLeaves.length,
            localIps: getLocalIpAddresses()
        });
        return;
    }

    // 2. Delta Pull: GET /api/sync/pull?since=<timestamp>
    if (pathname === '/api/sync/pull' && req.method === 'GET') {
        const since = parseInt(parsedUrl.query.since || '0', 10);
        const filteredRequests = db.requests.filter(r => 
            (r.timestampCreated || 0) > since ||
            (r.timestampFacultyAction || 0) > since ||
            (r.timestampAdminAction || 0) > since
        );
        const filteredLeaves = db.facultyLeaves.filter(l => (l.timestamp || 0) > since);
        const filteredSlots = db.timetableSlots.filter(s => (s.timestamp || 0) > since);
        const filteredSubjects = db.subjectUpdates.filter(s => (s.timestamp || 0) > since);

        sendJson(res, 200, {
            serverTime: Date.now(),
            requests: filteredRequests,
            timetableSlots: filteredSlots,
            facultyLeaves: filteredLeaves,
            subjectUpdates: filteredSubjects
        });
        return;
    }

    // 3. CR Submits Reschedule Request: POST /api/requests/submit
    if (pathname === '/api/requests/submit' && req.method === 'POST') {
        parseJsonBody(req, (err, data) => {
            if (err || !data || !data.syncId) {
                sendJson(res, 400, { error: 'Invalid request payload or missing syncId' });
                return;
            }

            const existingIdx = db.requests.findIndex(r => r.syncId === data.syncId);
            data.timestampCreated = data.timestampCreated || Date.now();
            if (existingIdx >= 0) {
                db.requests[existingIdx] = Object.assign(db.requests[existingIdx], data);
            } else {
                db.requests.unshift(data);
            }
            saveDb();
            console.log(`[CR REQUEST] Submitted: ${data.syncId} (${data.className} - ${data.subjectName})`);
            sendJson(res, 200, { success: true, syncId: data.syncId, timestamp: Date.now() });
        });
        return;
    }

    // 4. Faculty Action (Approve / Reject): POST /api/requests/faculty-action
    if (pathname === '/api/requests/faculty-action' && req.method === 'POST') {
        parseJsonBody(req, (err, data) => {
            if (err || !data || !data.syncId || !data.status) {
                sendJson(res, 400, { error: 'Missing syncId or status' });
                return;
            }

            const reqObj = db.requests.find(r => r.syncId === data.syncId);
            if (reqObj) {
                reqObj.status = data.status;
                reqObj.facultyRemarks = data.remarks || '';
                reqObj.timestampFacultyAction = data.timestamp || Date.now();
                saveDb();
                console.log(`[FACULTY ACTION] ${data.status} for ${data.syncId}: "${data.remarks}"`);
                sendJson(res, 200, { success: true, syncId: data.syncId });
            } else {
                sendJson(res, 404, { error: 'Request syncId not found' });
            }
        });
        return;
    }

    // 5. Admin Action (Authorize & Lock Timetable / Reject): POST /api/requests/admin-action
    if (pathname === '/api/requests/admin-action' && req.method === 'POST') {
        parseJsonBody(req, (err, data) => {
            if (err || !data || !data.syncId || !data.status) {
                sendJson(res, 400, { error: 'Missing syncId or status' });
                return;
            }

            const reqObj = db.requests.find(r => r.syncId === data.syncId);
            if (reqObj) {
                reqObj.status = data.status;
                reqObj.adminRemarks = data.remarks || '';
                reqObj.timestampAdminAction = data.timestamp || Date.now();

                // If Admin approved and locked slot, record slot and subject quota
                if (data.status === 'ADMIN_APPROVED' && data.newSlot) {
                    const slot = data.newSlot;
                    slot.timestamp = Date.now();
                    db.timetableSlots.push(slot);

                    if (data.classId && data.subjectName) {
                        const existingSubj = db.subjectUpdates.find(s => s.classId === data.classId && s.subjectName === data.subjectName);
                        if (existingSubj) {
                            existingSubj.scheduledLectures += 1;
                            existingSubj.pendingLectures = Math.max(0, existingSubj.pendingLectures - 1);
                            existingSubj.timestamp = Date.now();
                        } else {
                            db.subjectUpdates.push({
                                classId: data.classId,
                                subjectName: data.subjectName,
                                scheduledLectures: 1,
                                pendingLectures: 0,
                                timestamp: Date.now()
                            });
                        }
                    }
                }
                saveDb();
                console.log(`[ADMIN ACTION] ${data.status} for ${data.syncId}: "${data.remarks}"`);
                sendJson(res, 200, { success: true, syncId: data.syncId });
            } else {
                sendJson(res, 404, { error: 'Request syncId not found' });
            }
        });
        return;
    }

    // 6. Faculty Leave Intimation: POST /api/leaves/intimate
    if (pathname === '/api/leaves/intimate' && req.method === 'POST') {
        parseJsonBody(req, (err, data) => {
            if (err || !data || !data.facultyCode || !data.leaveDate) {
                sendJson(res, 400, { error: 'Invalid leave payload' });
                return;
            }
            data.timestamp = data.timestamp || Date.now();
            db.facultyLeaves.push(data);
            saveDb();
            console.log(`[FACULTY LEAVE] Faculty ${data.facultyCode} on ${data.leaveDate} (${data.reason})`);
            sendJson(res, 200, { success: true, leaveId: data.leaveId });
        });
        return;
    }

    // 7. Full Export
    if (pathname === '/api/export' && req.method === 'GET') {
        sendJson(res, 200, db);
        return;
    }

    // 8. Auth Credentials Reference Sheet
    if (pathname === '/api/auth/credentials' && req.method === 'GET') {
        sendJson(res, 200, {
            adminAccounts: [
                { email: "admin.stme@nmims.edu", role: "ADMIN", name: "STME Academic Administration", pass: "Admin@STME2026" },
                { email: "attendance.stme@nmims.edu", role: "ADMIN", name: "Central Attendance Monitoring Cell", pass: "Attendance@2026" },
                { email: "director.stme@nmims.edu", role: "ADMIN", name: "Dr. Anshuman Jaswal (Director)", pass: "Director@2026" }
            ],
            featuredFaculty: [
                { email: "vikas.khare@nmims.edu", code: "VK", role: "FACULTY", name: "Dr. Vikas Khare (Associate Dean)", pass: "Faculty@VK2026" },
                { email: "nidhhi.asthana@nmims.edu", code: "NA", role: "FACULTY", name: "Dr. Nidhhi Asthana", pass: "Faculty@NA2026" },
                { email: "lokesh.gupta@nmims.edu", code: "MJ", role: "FACULTY", name: "Mr. Lokesh Gupta", pass: "Faculty@MJ2026" }
            ],
            featuredCRs: [
                { email: "aarav.sharma@nmims.edu", classId: "BTECH_CE_S1_A", role: "CR", name: "Aarav Sharma", pass: "CR@CES1A" },
                { email: "kabir.singh@nmims.edu", classId: "BTECH_AIDS_S1_A", role: "CR", name: "Kabir Singh", pass: "CR@AIDSS1A" },
                { email: "yash.malhotra@nmims.edu", classId: "BTECH_CE_S5_A", role: "CR", name: "Yash Malhotra", pass: "CR@CES5A" }
            ]
        });
        return;
    }

    // Default 404
    sendJson(res, 404, { error: 'Endpoint not found', path: pathname });
});

server.listen(PORT, '0.0.0.0', () => {
    const ips = getLocalIpAddresses();
    console.log('================================================================');
    console.log('  NMIMS INDORE • STME ACADEMIC PENDENCY SCHEDULER');
    console.log('  Multi-Device Real-Time Synchronization Server');
    console.log('================================================================');
    console.log(`  ✓ Server listening on PORT : ${PORT}`);
    console.log(`  ✓ Android Emulator Address : http://10.0.2.2:${PORT}`);
    console.log('  ✓ Local Wi-Fi Addresses for Physical Devices:');
    ips.forEach(ip => console.log(`      -> http://${ip}:${PORT}`));
    console.log('================================================================');
});
