const fs = require('fs');
const path = require('path');

const csvPath = path.join(__dirname, 'raw_timetable.csv');
const content = fs.readFileSync(csvPath, 'utf8');
const lines = content.split('\n');

const pageToClass = {
    1: { id: "BTECH_CE_S1_A", name: "B.Tech. CE SEC A SEM I", prog: "CE", sem: 1, sec: "A", room: "CR 302", cr: "Aarav Sharma & Diya Patel" },
    2: { id: "BTECH_CE_S1_B", name: "B.Tech. CE SEC B SEM I", prog: "CE", sem: 1, sec: "B", room: "CR 303", cr: "Rohan Mehta & Ananya Joshi" },
    3: { id: "BTECH_AIDS_S1_B", name: "B.Tech. AIDS SEC B SEM I", prog: "AIDS", sem: 1, sec: "B", room: "CR 303", cr: "Ishaan Verma & Sneha Rao" },
    4: { id: "BTECH_AIDS_S1_A", name: "B.Tech. AIDS SEM I SEC A", prog: "AIDS", sem: 1, sec: "A", room: "CR 304", cr: "Kabir Singh & Tanvi Shah" },
    5: { id: "MBATECH_CE_S1", name: "MBA.Tech. CE SEM I", prog: "MBA.Tech", sem: 1, sec: "A", room: "CR 306", cr: "Vivaan Gupta & Riya Desai" },
    6: { id: "BTECH_CE_S3_A", name: "B.Tech. CE SEC A SEM III", prog: "CE", sem: 3, sec: "A", room: "CR 307", cr: "Aditya Nair & Meera Kulkarni" },
    7: { id: "BTECH_CE_S3_B", name: "B.Tech. CE SEC B SEM III", prog: "CE", sem: 3, sec: "B", room: "CR 308", cr: "Devansh Dave & Pooja Bhatt" },
    8: { id: "BTECH_AIDS_S3", name: "B.Tech. AIDS SEM III", prog: "AIDS", sem: 3, sec: "A", room: "CR 309", cr: "Arjun Reddy & Nikita Sen" },
    9: { id: "MBATECH_CE_S3", name: "MBA.Tech. CE SEM III", prog: "MBA.Tech", sem: 3, sec: "A", room: "CR 310", cr: "Siddharth Jain & Shreya Trivedi" },
    10: { id: "BTECH_CE_S5_A", name: "B.Tech. CE SEC A SEM V", prog: "CE", sem: 5, sec: "A", room: "CR 311", cr: "Yash Malhotra & Kritika Saxena" },
    11: { id: "BTECH_CE_S5_B", name: "B.Tech. CE SEC B SEM V", prog: "CE", sem: 5, sec: "B", room: "CR 312", cr: "Harsh Vardhan & Ishita Roy" },
    12: { id: "MBATECH_CE_S5", name: "MBA.Tech. CE SEM V", prog: "MBA.Tech", sem: 5, sec: "A", room: "CR 315", cr: "Kunal Chopra & Avni Agarwal" },
    13: { id: "BTECH_AIDS_S5_A", name: "B.Tech. AIDS SEC A SEM V", prog: "AIDS", sem: 5, sec: "A", room: "CR 313", cr: "Parth Solanki & Bhavya Mittal" },
    14: { id: "BTECH_AIDS_S5_B", name: "B.Tech. AIDS SEC B SEM V", prog: "AIDS", sem: 5, sec: "B", room: "CR 314", cr: "Manav Seth & Sanya Kapoor" },
    15: { id: "BTECH_CE_S7_A", name: "B.Tech. CE SEM VII Sec A", prog: "CE", sem: 7, sec: "A", room: "CR 316", cr: "Nikhil Bansal & Payal Pandey" },
    16: { id: "BTECH_CE_S7_B", name: "B.Tech. CE SEM VII Sec B", prog: "CE", sem: 7, sec: "B", room: "CR 317", cr: "Gaurav Shinde & Anoushka Nair" },
    17: { id: "MBATECH_CE_S7", name: "MBA.Tech. CE SEM VII", prog: "MBA.Tech", sem: 7, sec: "A", room: "CR 413", cr: "Varun Ahuja & Priyanka Choudhary" },
    18: { id: "BTECH_AIDS_S7", name: "B.Tech. AIDS SEM VII", prog: "AIDS", sem: 7, sec: "A", room: "CR 301", cr: "Dhruv Tiwari & Mansi Goyal" }
};

const facultyCodes = ["NA", "AR", "VJ", "VK", "JG", "SS", "JP", "MJ", "PS", "AC", "SV", "HG", "DG", "NiG", "DS", "RD", "SO", "AP", "RR", "PST", "PM", "KJ", "RGM", "GP", "MB", "SJ", "ADS", "NG", "AS", "MG", "SG", "AM", "CP", "Hgo"];

function normalizeDay(day) {
    if (!day) return "Mon";
    const d = day.trim().toLowerCase();
    if (d.startsWith("mon")) return "Mon";
    if (d.startsWith("tue")) return "Tues";
    if (d.startsWith("wed")) return "Wed";
    if (d.startsWith("thu")) return "Thurs";
    if (d.startsWith("fri")) return "Fri";
    if (d.startsWith("sat")) return "Sat";
    return "Mon";
}

function normalizeTimeSlot(slot) {
    if (!slot) return { start: "09:00", end: "10:00", display: "9:00 am to 10:00 am" };
    const s = slot.trim();
    if (s.includes("09:00") || s.includes("9:00")) return { start: "09:00", end: "10:00", display: "9:00 am to 10:00 am" };
    if (s.includes("10:00")) return { start: "10:00", end: "11:00", display: "10:00 am to 11:00 am" };
    if (s.includes("11:00") || s.includes("11:10")) return { start: "11:10", end: "12:10", display: "11:10 am to 12:10 pm" };
    if (s.includes("11:50") || s.includes("12:10")) return { start: "11:50", end: "12:50", display: "11:50 am to 12:50 pm" };
    if (s.includes("12:50")) return { start: "12:50", end: "13:50", display: "12:50 pm to 1:50 pm" };
    if (s.includes("14:10") || s.includes("2:10")) return { start: "14:10", end: "15:10", display: "2:10 pm to 3:10 pm" };
    if (s.includes("15:10") || s.includes("3:10")) return { start: "15:10", end: "16:10", display: "3:10 pm to 4:10 pm" };
    return { start: "09:00", end: "10:00", display: "9:00 am to 10:00 am" };
}

const extractedSlots = [];
const seenKeys = new Set();

for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim();
    if (!line) continue;
    const parts = line.split(',');
    if (parts.length < 11) continue;

    const sourcePage = parseInt(parts[10].trim(), 10);
    const classInfo = pageToClass[sourcePage];
    if (!classInfo) continue;

    const day = normalizeDay(parts[7]);
    const time = normalizeTimeSlot(parts[8]);
    let rawText = parts[9].trim();

    // Skip mentor mentee / administrative noise
    if (rawText.toLowerCase().includes("mentor mentee") || 
        rawText.toLowerCase().includes("dean and prof") || 
        rawText.toLowerCase().includes("nmims indore") || 
        rawText.toLowerCase().includes("director") ||
        rawText.toLowerCase().includes("coordinator")) {
        continue;
    }

    // Extract faculty
    let faculty = "NA";
    for (const fc of facultyCodes) {
        const regex = new RegExp(`\\b${fc}\\b`);
        if (regex.test(rawText)) {
            faculty = fc === "Hgo" ? "HG" : fc;
            break;
        }
    }

    // Extract room
    let room = classInfo.room;
    const roomMatches = rawText.match(/(CR\s*\d+|CC\s*\d+|CC-\s*\d+|CL\s*\d+|EI\s*lab|BEE\s*Lab|LL\s*\d+|TUT\s*\d+)/i);
    if (roomMatches) {
        room = roomMatches[1].toUpperCase().replace("-", " ").replace(/\s+/, " ");
    }

    // Clean subject name
    let subject = rawText
        .replace(/(CR\s*\d+|CC\s*\d+|CC-\s*\d+|CL\s*\d+|EI\s*lab|BEE\s*Lab|LL\s*\d+|TUT\s*\d+)/gi, '')
        .replace(/\b(NA|AR|VJ|VK|JG|SS|JP|MJ|PS|AC|SV|HG|DG|NiG|DS|RD|SO|AP|RR|PST|PM|KJ|RGM|GP|MB|SJ|ADS|NG|AS|MG|SG|AM|CP|Hgo)\b/g, '')
        .replace(/Batch\s*\d+/gi, '')
        .replace(/C o m p u ta t io n a l/gi, 'Computational')
        .replace(/T h in k i n g/gi, 'Thinking')
        .replace(/f o r/gi, 'for')
        .replace(/P r ob le m/gi, 'Problem')
        .replace(/S o lv i n g/gi, 'Solving')
        .replace(/T h e o r y/gi, 'Theory')
        .replace(/T h e or y/gi, 'Theory')
        .replace(/Th e o ry/gi, 'Theory')
        .replace(/T he o r y/gi, 'Theory')
        .replace(/T h e o ry/gi, 'Theory')
        .replace(/L a b/gi, 'Lab')
        .replace(/P r ac t ic e s/gi, 'Practices')
        .replace(/E le c tr o n i cs/gi, 'Electronics')
        .replace(/E s s e n t ia l/gi, 'Essential')
        .replace(/E n g i n e e r i n g/gi, 'Engineering')
        .replace(/G r a p h i c s/gi, 'Graphics')
        .replace(/G ra p h i cs/gi, 'Graphics')
        .replace(/D e s i g n/gi, 'Design')
        .replace(/D e s ig n/gi, 'Design')
        .replace(/Indian K n o w le d g e System/gi, 'Indian Knowledge System')
        .replace(/Indian K n o w l e d g e System/gi, 'Indian Knowledge System')
        .replace(/Discrete Ma t h e m a t i cs/gi, 'Discrete Mathematics')
        .replace(/Discrete Math e matics/gi, 'Discrete Mathematics')
        .replace(/Data Structurs and Algorithms/gi, 'Data Structures and Algorithms')
        .replace(/Struc tu r s a n d A lgorithms/gi, 'Data Structures and Algorithms')
        .replace(/C om p u t e r O r g a n i za t i o n/gi, 'Computer Organization')
        .replace(/C o m p u te r O r g a ni z a t io n/gi, 'Computer Organization')
        .replace(/a nd/gi, 'and')
        .replace(/a n d/gi, 'and')
        .replace(/A rc h it e c t u r e/gi, 'Architecture')
        .replace(/A r ch i te c tu re/gi, 'Architecture')
        .replace(/Pr in c ip l e s of E c o n o m i cs/gi, 'Principles of Economics')
        .replace(/Pr in c ip l e/gi, 'Principles')
        .replace(/M a n a g e m e n t/gi, 'Management')
        .replace(/Program m i n g w it h Python/gi, 'Programming with Python')
        .replace(/Signals and Systems/gi, 'Signals and Systems')
        .replace(/Probability and Statistics/gi, 'Probability and Statistics')
        .replace(/Probabi li ty a n d S tatistics/gi, 'Probability and Statistics')
        .replace(/Statistical Learning/gi, 'Statistical Learning')
        .replace(/Signal and Image Processing/gi, 'Signal and Image Processing')
        .replace(/Data Visualization and Interpretation/gi, 'Data Visualization and Interpretation')
        .replace(/Fundamental of Operating Systems/gi, 'Operating Systems')
        .replace(/Operating Systems/gi, 'Operating Systems')
        .replace(/Software Engineering/gi, 'Software Engineering')
        .replace(/Artificial Intelligence/gi, 'Artificial Intelligence')
        .replace(/Mobile Application Development/gi, 'Mobile Application Development')
        .replace(/Image and Video Processing/gi, 'Image and Video Processing')
        .replace(/Image an d V i d e o P rocessing/gi, 'Image and Video Processing')
        .replace(/Cloud Computing/gi, 'Cloud Computing')
        .replace(/Deep Learning/gi, 'Deep Learning')
        .replace(/Big Data Analytics/gi, 'Big Data Analytics')
        .replace(/Cryptography and Network Security/gi, 'Cryptography and Network Security')
        .replace(/Natural Language Processing/gi, 'Natural Language Processing')
        .replace(/Turning data into story/gi, 'Turning Data into Story')
        .replace(/Operations Management/gi, 'Operations Management')
        .replace(/Business Statistics/gi, 'Business Statistics')
        .replace(/Marketing Management/gi, 'Marketing Management')
        .replace(/Financial Accounting & Analysis/gi, 'Financial Accounting and Analysis')
        .replace(/Applied Time Series Analysis/gi, 'Applied Time Series Analysis')
        .replace(/Human Computer Interaction/gi, 'Human Computer Interaction')
        .replace(/Advanced topic in Machine Learning/gi, 'Advanced Topics in Machine Learning')
        .replace(/Advanced Topics in Machine Learning/gi, 'Advanced Topics in Machine Learning')
        .replace(/\s+/g, ' ')
        .trim();

    if (!subject || subject.length < 3) continue;

    const key = `${classInfo.id}_${day}_${time.start}_${faculty}`;
    if (seenKeys.has(key)) continue;
    seenKeys.add(key);

    extractedSlots.push({
        classId: classInfo.id,
        day: day,
        start: time.start,
        end: time.end,
        display: time.display,
        subject: subject,
        faculty: faculty,
        room: room,
        batch: rawText.includes("Batch 01") ? "Batch 01" : (rawText.includes("Batch 02") ? "Batch 02" : "All"),
        slotType: "REGULAR"
    });
}

console.log(`Parsed ${extractedSlots.length} distinct timetable slots from all 18 classes!`);
fs.writeFileSync(path.join(__dirname, 'extracted_slots.json'), JSON.stringify(extractedSlots, null, 2));
