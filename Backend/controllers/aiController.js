const axios = require('axios');
const Application = require('../models/Application');
const Fir = require('../models/Fir');

/**
 * Local ML API Base URL
 */
const ML_API_URL = process.env.ML_API_URL || 'http://localhost:8000';

/**
 * Helper to Extract Keywords for Detail (Local Lexical Analysis)
 */
const extractKeywords = (text) => {
    const keywords = ['weapon', 'threat', 'assault', 'theft', 'robbery', 'murder', 'fraud', 'cheat', 'online', 'phone', 'money', 'night', 'vehicle', 'car', 'bike', 'motorcycle'];
    return keywords.filter(kw => text.toLowerCase().includes(kw));
};

/**
 * Suggest Acts using Custom Trained Model
 */
exports.suggestActs = async (req, res) => {
    try {
        const { applicationText } = req.body;
        if (!applicationText) {
            return res.status(400).json({ success: false, message: 'Application text is required' });
        }

        console.log(`[ML] Calling local ML model for: ${applicationText.substring(0, 50)}...`);
        
        try {
            const mlResponse = await axios.post(`${ML_API_URL}/predict`, { 
                text: applicationText 
            });

            if (mlResponse.data && mlResponse.data.success) {
                const act = mlResponse.data.prediction;
                console.log(`[ML] Prediction Successful: ${act.actName}`);
                return res.json({ 
                    success: true, 
                    suggestedActs: [act], 
                    source: 'Local ML Model'
                });
            }
        } catch (mlErr) {
            console.warn(`[ML] Local ML model failed: ${mlErr.message}`);
            const errorMsg = mlErr.response?.data?.message || mlErr.message;
            return res.status(503).json({ 
                success: false, 
                message: `Local ML Service Error: ${errorMsg}. Please ensure the 'legal_transformer_model' folder exists and restart the Python server.` 
            });
        }
    } catch (error) {
        console.error('Final Error (Suggest Acts):', error.message);
        res.status(500).json({ success: false, message: 'Internal Server Error' });
    }
};

/**
 * Helper to Extract Incident Description from structured FIR text
 */
const extractIncidentDescription = (text) => {
    if (!text) return "";
    // Match fields like "Description of the Incident" or similar and extract content until next field or end of text
    const match = text.match(/(?:Description of the Incident|Description of Incident|Incident Description|Description)[\s:]+([\s\S]+?)(?=(?:Medical report:|Delay in Reporting:|Action Taken:|Signature of Complainant:|Name and Rank of Officer|\[|$))/i);
    if (match && match[1]) {
        return match[1].trim();
    }
    return text;
};

// Dictionaries for standard IPC-to-BNS and BNS-to-IPC conversions
const IPC_TO_BNS_MAP = {
    "323": { bns: "BNS 115(2)", category: "Assault" },
    "326": { bns: "BNS 117(2)", category: "Assault" },
    "324": { bns: "BNS 117(1)", category: "Assault" },
    "504": { bns: "BNS 352", category: "Assault" },
    "34": { bns: "BNS 3(5)", category: "Common Intention" },
    "378": { bns: "BNS 303", category: "Theft" },
    "379": { bns: "BNS 303(2)", category: "Theft" },
    "302": { bns: "BNS 101", category: "Murder" },
    "375": { bns: "BNS 64", category: "Rape" },
    "376": { bns: "BNS 64", category: "Rape" },
    "420": { bns: "BNS 318(4)", category: "Cheating/Fraud" },
    "390": { bns: "BNS 309", category: "Robbery" },
    "441": { bns: "BNS 329", category: "Burglary" },
    "359": { bns: "BNS 137", category: "Kidnapping" },
    "171E": { bns: "BNS 170", category: "Bribery" },
    "383": { bns: "BNS 308", category: "Extortion" },
    "498A": { bns: "BNS 85", category: "Dowry Harassment" },
    "499": { bns: "BNS 356", category: "Defamation" },
    "279": { bns: "BNS 281", category: "Hit and Run" },
    "304A": { bns: "BNS 106(2)", category: "Hit and Run" },
    "447": { bns: "BNS 329", category: "Trespass" },
    "463": { bns: "BNS 335", category: "Forgery" },
    "465": { bns: "BNS 336", category: "Forgery" },
    "468": { bns: "BNS 336(3)", category: "Forgery" },
    "471": { bns: "BNS 340(2)", category: "Forgery" },
    "268": { bns: "BNS 270", category: "Public Nuisance" }
};

const BNS_TO_IPC_MAP = {
    "115(2)": { ipc: "IPC 323", category: "Assault" },
    "115": { ipc: "IPC 323", category: "Assault" },
    "117(2)": { ipc: "IPC 326", category: "Assault" },
    "117(1)": { ipc: "IPC 324", category: "Assault" },
    "117": { ipc: "IPC 324/326", category: "Assault" },
    "352": { ipc: "IPC 504", category: "Assault" },
    "3(5)": { ipc: "IPC 34", category: "Common Intention" },
    "303": { ipc: "IPC 378", category: "Theft" },
    "303(2)": { ipc: "IPC 379", category: "Theft" },
    "101": { ipc: "IPC 302", category: "Murder" },
    "64": { ipc: "IPC 375/376", category: "Rape" },
    "318(4)": { ipc: "IPC 420", category: "Cheating/Fraud" },
    "309": { ipc: "IPC 390", category: "Robbery" },
    "329": { ipc: "IPC 441/447", category: "Burglary/Trespass" },
    "137": { ipc: "IPC 359", category: "Kidnapping" },
    "170": { ipc: "IPC 171E", category: "Bribery" },
    "308": { ipc: "IPC 383", category: "Extortion" },
    "85": { ipc: "IPC 498A", category: "Dowry Harassment" },
    "356": { ipc: "IPC 499", category: "Defamation" },
    "281": { ipc: "IPC 279", category: "Hit and Run" },
    "106(2)": { ipc: "IPC 304A", category: "Hit and Run" },
    "335": { ipc: "IPC 463", category: "Forgery" },
    "336": { ipc: "IPC 465/468", category: "Forgery" },
    "340(2)": { ipc: "IPC 471", category: "Forgery" },
    "270": { ipc: "IPC 268", category: "Public Nuisance" }
};

/**
 * Global Helper for FIR Comparison (Audit Report)
 * Used by both Direct Comparison and Offline PDF Upload
 */
exports.generateComparisonReport = async (originalDesc, firText, sourceName = "Local AI", applicationSuggestedActs = [], firAppliedActs = []) => {
    try {
        // Extract plain incident descriptions to feed to ML model
        const cleanOriginalDesc = extractIncidentDescription(originalDesc);
        const cleanFirText = extractIncidentDescription(firText);

        console.log(`[ML Compare] Clean Original Desc: ${cleanOriginalDesc.substring(0, 100)}...`);
        console.log(`[ML Compare] Clean FIR Text: ${cleanFirText.substring(0, 100)}...`);

        // 1. Predict for clean incident descriptions
        const appRes = await axios.post(`${ML_API_URL}/predict`, { text: cleanOriginalDesc });
        let appActs = appRes.data.prediction;

        const firRes = await axios.post(`${ML_API_URL}/predict`, { text: cleanFirText });
        let firActs = firRes.data.prediction;

        // Helper to convert DB acts list to prediction format
        const convertActsListToPrediction = (actsList, mlPredFallback) => {
            if (!actsList || actsList.length === 0) {
                return null;
            }

            let explicitIpcs = [];
            let explicitBns = [];
            let categories = [];

            const simpleIpcRegex = /IPC\s*([0-9]+[A-Z]?(?:\/[0-9]+[A-Z]?)*)/gi; 
            const simpleBnsRegex = /BNS\s*([0-9]+[A-Z]?(?:\([0-9]+\))?(?:\/[0-9]+[A-Z]?(?:\([0-9]+\))?)*)/gi;

            for (let act of actsList) {
                const name = act.actName || "";
                let match;
                
                // Reset regex indices
                simpleIpcRegex.lastIndex = 0;
                simpleBnsRegex.lastIndex = 0;

                while ((match = simpleIpcRegex.exec(name)) !== null) {
                    explicitIpcs.push(match[1].toUpperCase());
                }
                while ((match = simpleBnsRegex.exec(name)) !== null) {
                    explicitBns.push(match[1].toUpperCase());
                }

                // If no prefix but it matches mappings
                if (!name.toUpperCase().includes("IPC") && !name.toUpperCase().includes("BNS")) {
                    const parts = name.split(/[\s,/;]+/);
                    for (let part of parts) {
                        part = part.trim().toUpperCase();
                        if (part && /[0-9]/.test(part)) {
                            if (IPC_TO_BNS_MAP[part]) {
                                explicitIpcs.push(part);
                                explicitBns.push(IPC_TO_BNS_MAP[part].bns.replace(/^BNS\s*/i, ''));
                                categories.push(IPC_TO_BNS_MAP[part].category);
                            } else if (BNS_TO_IPC_MAP[part]) {
                                explicitBns.push(part);
                                explicitIpcs.push(BNS_TO_IPC_MAP[part].ipc.replace(/^IPC\s*/i, ''));
                                categories.push(BNS_TO_IPC_MAP[part].category);
                            }
                        }
                    }
                }
            }

            // Cross reference mapped sections to complete the pairs
            for (let i = 0; i < explicitIpcs.length; i++) {
                const sec = explicitIpcs[i].replace(/^IPC\s*/i, '').trim();
                if (IPC_TO_BNS_MAP[sec]) {
                    explicitBns.push(IPC_TO_BNS_MAP[sec].bns.replace(/^BNS\s*/i, ''));
                    categories.push(IPC_TO_BNS_MAP[sec].category);
                }
            }
            for (let i = 0; i < explicitBns.length; i++) {
                const sec = explicitBns[i].replace(/^BNS\s*/i, '').trim();
                if (BNS_TO_IPC_MAP[sec]) {
                    explicitIpcs.push(BNS_TO_IPC_MAP[sec].ipc.replace(/^IPC\s*/i, ''));
                    categories.push(BNS_TO_IPC_MAP[sec].category);
                }
            }

            explicitIpcs = [...new Set(explicitIpcs)];
            explicitBns = [...new Set(explicitBns)];

            const formatSections = (prefix, list) => {
                const numbers = list.map(item => item.replace(new RegExp(`^${prefix}\\s*`, 'i'), '').trim());
                const uniqueNumbers = [...new Set(numbers)];
                return `${prefix} ${uniqueNumbers.join(', ')}`;
            };

            const result = {
                ipc_section: explicitIpcs.length > 0 ? formatSections('IPC', explicitIpcs) : (mlPredFallback?.ipc_section || "N/A"),
                bns_section: explicitBns.length > 0 ? formatSections('BNS', explicitBns) : (mlPredFallback?.bns_section || "N/A"),
                explicit_sections: true
            };
            result.actName = `${result.ipc_section} / ${result.bns_section}`;

            if (categories.length > 0) {
                const filtered = categories.filter(c => !["Common Intention", "Defamation/Insult"].includes(c));
                const finalCategory = filtered.length > 0 ? filtered[0] : categories[0];
                result.crime_category = finalCategory;
                result.explicit_category = true;
            } else {
                result.crime_category = mlPredFallback?.crime_category || "General Incident";
                result.explicit_category = false;
            }

            return result;
        };

        // Helper to extract explicit data written in the text
        const extractExplicitData = (text, mlPred) => {
            const result = { ...mlPred };
            
            const simpleIpcRegex = /IPC\s*([0-9]+[A-Z]?(?:\/[0-9]+[A-Z]?)*)/gi; 
            const simpleBnsRegex = /BNS\s*([0-9]+[A-Z]?(?:\([0-9]+\))?(?:\/[0-9]+[A-Z]?(?:\([0-9]+\))?)*)/gi;

            let explicitIpcs = [];
            let explicitBns = [];
            let match;
            
            while ((match = simpleIpcRegex.exec(text)) !== null) {
                explicitIpcs.push(match[1].toUpperCase());
            }
            while ((match = simpleBnsRegex.exec(text)) !== null) {
                explicitBns.push(match[1].toUpperCase());
            }

            // Parse raw section list like "Section:326,323,504,34"
            const sectionFieldRegex = /(?:Section|Sections|Offence Details|Applied Acts)[\s:]*(?:section|sections)?[\s:]*([0-9A-Za-z,\s/()]+)/gi;
            let sectionFieldMatch;
            let rawSections = [];
            
            while ((sectionFieldMatch = sectionFieldRegex.exec(text)) !== null) {
                const parts = sectionFieldMatch[1].split(/[\s,]+/);
                for (let part of parts) {
                    part = part.trim().toUpperCase();
                    if (part && !/^(AND|OR|OF|THE|TO|FOR|BY|WITH)$/i.test(part)) {
                        part = part.replace(/^[^A-Z0-9(]+|[^A-Z0-9)]+$/g, '');
                        if (part && /[0-9]/.test(part)) {
                            rawSections.push(part);
                        }
                    }
                }
            }

            let categories = [];
            for (let sec of rawSections) {
                if (IPC_TO_BNS_MAP[sec]) {
                    explicitIpcs.push(`IPC ${sec}`);
                    explicitBns.push(IPC_TO_BNS_MAP[sec].bns);
                    categories.push(IPC_TO_BNS_MAP[sec].category);
                } else if (BNS_TO_IPC_MAP[sec]) {
                    explicitBns.push(`BNS ${sec}`);
                    explicitIpcs.push(BNS_TO_IPC_MAP[sec].ipc);
                    categories.push(BNS_TO_IPC_MAP[sec].category);
                } else {
                    explicitIpcs.push(`IPC ${sec}`);
                    explicitBns.push(`BNS ${sec}`);
                }
            }

            // Cross reference mapped sections to complete the pairs
            for (let i = 0; i < explicitIpcs.length; i++) {
                const sec = explicitIpcs[i].replace(/^IPC\s*/i, '').trim();
                if (IPC_TO_BNS_MAP[sec]) {
                    explicitBns.push(IPC_TO_BNS_MAP[sec].bns);
                    categories.push(IPC_TO_BNS_MAP[sec].category);
                }
            }
            for (let i = 0; i < explicitBns.length; i++) {
                const sec = explicitBns[i].replace(/^BNS\s*/i, '').trim();
                if (BNS_TO_IPC_MAP[sec]) {
                    explicitIpcs.push(BNS_TO_IPC_MAP[sec].ipc);
                    categories.push(BNS_TO_IPC_MAP[sec].category);
                }
            }

            explicitIpcs = [...new Set(explicitIpcs)];
            explicitBns = [...new Set(explicitBns)];
            
            if (explicitIpcs.length > 0 || explicitBns.length > 0) {
                const formatSections = (prefix, list) => {
                    const numbers = list.map(item => item.replace(new RegExp(`^${prefix}\\s*`, 'i'), '').trim());
                    const uniqueNumbers = [...new Set(numbers)];
                    return `${prefix} ${uniqueNumbers.join(', ')}`;
                };

                result.ipc_section = explicitIpcs.length > 0 ? formatSections('IPC', explicitIpcs) : result.ipc_section;
                result.bns_section = explicitBns.length > 0 ? formatSections('BNS', explicitBns) : result.bns_section;
                result.explicit_sections = true;
            }

            const categoryRegex = /SUBJECT:\s*FORMAL COMPLAINT REGARDING\s*([^\n]+)/i;
            const catMatch = categoryRegex.exec(text);
            if (catMatch && catMatch[1]) {
                const cat = catMatch[1].trim();
                if (cat.length < 50) {
                    result.crime_category = cat;
                    result.explicit_category = true;
                }
            }

            if (categories.length > 0) {
                const filtered = categories.filter(c => !["Common Intention", "Defamation/Insult"].includes(c));
                const finalCategory = filtered.length > 0 ? filtered[0] : categories[0];
                result.crime_category = finalCategory;
                result.explicit_category = true;
            }
            
            return result;
        };

        // Populate appActs (AI Generated Section)
        const appActsDb = convertActsListToPrediction(applicationSuggestedActs, appActs);
        if (appActsDb) {
            appActs = { ...appActs, ...appActsDb };
        }
        // Note: We do NOT extract explicit sections from originalDesc for the AI column,
        // so that it represents the pure AI prediction/recommendation.

        // Populate firActs (Police Direct Draft Fir Section)
        // We prioritize explicit sections written in the FIR text.
        // If not found in the text, we fall back to database applied acts.
        // If that is also empty, we fall back to raw prediction.
        const explicitFirActs = extractExplicitData(firText, firActs);
        if (explicitFirActs.explicit_sections) {
            firActs = explicitFirActs;
        } else {
            const firActsDb = convertActsListToPrediction(firAppliedActs, firActs);
            if (firActsDb) {
                firActs = { ...firActs, ...firActsDb };
            } else {
                firActs = explicitFirActs; // fallback
            }
        }

        // 2. Keyword check
        const appKws = extractKeywords(originalDesc);
        const firKws = extractKeywords(firText);
        const missingKws = appKws.filter(kw => !firKws.includes(kw));

        // 3. Match Logic
        const cleanStr = (s) => (s || "").toString().trim().toLowerCase();
        const categoryMatch = (cleanStr(appActs.crime_category) === cleanStr(firActs.crime_category));
        
        let sectionMatch = false;
        if (appActs.explicit_sections || firActs.explicit_sections) {
            const norm = (s) => (s||"").toString().replace(/(IPC|BNS|\s)/gi, '').toLowerCase();
            sectionMatch = (norm(appActs.ipc_section) === norm(firActs.ipc_section)) &&
                           (norm(appActs.bns_section) === norm(firActs.bns_section));
        } else {
            sectionMatch = (appActs.actName === firActs.actName);
        }

        // 4. Accuracy Scoring Logic
        let legalScore = sectionMatch ? 40 : 0;
        let categoryScore = categoryMatch ? 30 : 0;
        
        let keywordScore = 0;
        if (appKws.length > 0) {
            const foundCount = appKws.length - missingKws.length;
            keywordScore = Math.round((foundCount / appKws.length) * 30);
        } else {
            // Fallback for simple descriptions without keywords
            keywordScore = firText.length > 20 ? 30 : 0;
        }

        const accuracyScore = legalScore + categoryScore + keywordScore;

        const summaryText = `**Overall Model Accuracy: ${accuracyScore}%**\n\n` + 
            (categoryMatch && sectionMatch && missingKws.length === 0
            ? 'The FIR is a 100% match for the original application both in facts and legal provisions.' 
            : `This FIR may be legally weak. ${!categoryMatch ? 'The category is inconsistent. ' : ''}${missingKws.length > 0 ? 'Critical facts like "' + missingKws.join(', ') + '" are missing from the formal text.' : ''}`);

        return {
            status: categoryMatch && sectionMatch ? 'Verified' : 'Discrepancy',
            accuracyScore: accuracyScore,
            originalCategory: appActs.crime_category,
            firCategory: firActs.crime_category,
            categoryMatch: categoryMatch,
            recommendedIpc: appActs.ipc_section,
            recommendedBns: appActs.bns_section,
            firIpcBns: `${firActs.ipc_section} / ${firActs.bns_section}`,
            sectionMatch: sectionMatch,
            keyConcepts: appKws,
            missingConcepts: missingKws,
            summaryText: summaryText,
            originalDesc: originalDesc,
            firText: firText,
            source: sourceName
        };
    } catch (err) {
        console.error("INNER ERROR IN generateComparisonReport:", err);
        throw new Error(`Comparison Logic Failed: ${err.message}`);
    }
};

/**
 * Enhanced Local FIR Comparison (Detailed Audit Route)
 */
exports.compareFir = async (req, res) => {
    try {
        const { applicationId } = req.params;
        const application = await Application.findById(applicationId);
        if (!application) return res.status(404).json({ success: false, message: 'Application not found' });
        
        const fir = await Fir.findOne({ applicationId });
        if (!fir) return res.status(404).json({ success: false, message: 'FIR not found for this application' });

        console.log(`[ML] Performing DETAILED Comparison via Local ML Helper...`);

        const reportObject = await exports.generateComparisonReport(
            application.description,
            fir.firText,
            "Local AI Audit",
            application.suggestedActs,
            fir.appliedActs
        );

        // Store as string for DB compatibility or update schema? 
        // For now, let's keep it flexible and send the object to the frontend.
        fir.aiComparisonReport = reportObject.summaryText; // Keep simple summary in DB
        await fir.save();

        res.json({ success: true, report: reportObject });
    } catch (error) {
        console.error('Local ML Error (Detailed Compare):', error.message);
        res.status(500).json({ success: false, message: 'Detailed Comparison Failed' });
    }
};

/**
 * Draft FIR using Local Templates and ML Prediction
 */
exports.draftFir = async (req, res) => {
    try {
        const { applicationId } = req.params;
        const application = await Application.findById(applicationId);
        if (!application) return res.status(404).json({ success: false, message: 'Application not found' });

        console.log(`[ML] Drafting FIR for application: ${applicationId}`);
        
        let crimes = { category: "Incident", ipc: "N/A", bns: "N/A" };
        try {
            const mlResponse = await axios.post(`${ML_API_URL}/predict`, { 
                text: application.description 
            });
            if (mlResponse.data && mlResponse.data.success) {
                const pred = mlResponse.data.prediction;
                crimes = {
                    category: pred.crime_category,
                    ipc: pred.ipc_section,
                    bns: pred.bns_section
                };
            }
        } catch (mlErr) {
            console.warn(`[ML] Prediction failed for draft: ${mlErr.message}`);
        }

        const today = new Date().toLocaleDateString('en-IN');
        const draftedText = `TO,
STATION HOUSE OFFICER,
LOCAL POLICE STATION,
[DISTRICT/STATE]

DATE: ${today}

SUBJECT: FORMAL COMPLAINT REGARDING ${crimes.category.toUpperCase()}

SIR/MAM,

I AM WRITING TO LODGE A FORMAL COMPLAINT REGARDING AN INCIDENT THAT OCCURRED RECENTLY.
THE DETAILS ARE AS FOLLOWS:

INCIDENT DESCRIPTION:
"${application.description}"

LEGAL PROVISIONS:
BASED ON THE FACTS, THIS INCIDENT PRIMARILY FALLS UNDER:
1. ${crimes.ipc} (INDIAN PENAL CODE)
2. ${crimes.bns} (BHARATIYA NYAYA SANHITA)

I REQUEST YOU TO REGISTER THIS COMPLAINT AS A FIRST INFORMATION REPORT (FIR) AND INITIATE A FORMAL INVESTIGATION TO ENSURE JUSTICE.

SINCERELY,
[NAME OF COMPLAINANT]
[CONTACT DETAILS]`;

        res.json({ 
            success: true, 
            draftedText, 
            acts: [crimes.ipc, crimes.bns],
            source: 'Local Template System'
        });

    } catch (error) {
        console.error('Final Error (Drafting):', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

/**
 * Local AI Chat - Repurposed as Detailed Legal Assistant
 */
exports.chat = async (req, res) => {
    try {
        const { message } = req.body;
        if (!message) return res.status(400).json({ success: false, message: 'Message is required' });

        console.log(`[ML] Chatting via local ML model for: ${message.substring(0, 30)}...`);

        try {
            const mlResponse = await axios.post(`${ML_API_URL}/predict`, { text: message });
            
            if (mlResponse.data && mlResponse.data.success) {
                // Check if it's a conversational greeting
                if (mlResponse.data.is_greeting) {
                    return res.json({ success: true, reply: mlResponse.data.reply });
                }

                const pred = mlResponse.data.prediction;
                const reply = `According to your description, this incident primarily falls under the category of **${pred.crime_category}**. 

**Relevant Legal Provisions:**
- **IPC Section:** ${pred.ipc_section}
- **BNS Section:** ${pred.bns_section}

**Advice:** You should file a formal complaint at your local police station. Use the "Fill Application" feature in this app to generate a professional draft. 

How else can I assist you with legal titles or IPC/BNS sections?`;

                return res.json({ success: true, reply: reply });
            }
        } catch (mlErr) {
            console.warn(`[ML] Chat bot fallback: ${mlErr.message}`);
            return res.json({ 
                success: true, 
                reply: "I am your local Legal AI. Please describe an incident (e.g., 'My phone was stolen') and I will tell you the exact IPC and BNS sections."
            });
        }
    } catch (error) {
        console.error('Local ML Error (Chat):', error.message);
        res.status(500).json({ success: false, message: 'Chatbot Error' });
    }
};

/**
 * Verify Application using Local Logic
 */
exports.verifyApplication = async (req, res) => {
    try {
        const { applicationText } = req.body;
        if (!applicationText) {
            return res.status(400).json({ success: false, message: 'Application text is required' });
        }

        console.log(`[ML] Verifying application completeness...`);

        const mlResponse = await axios.post(`${ML_API_URL}/predict`, { text: applicationText });

        const isComplete = applicationText.length > 30 && mlResponse.data.success;
        const missingPoints = [];

        if (applicationText.length <= 30) {
            missingPoints.push("Detailed description of the event flow.");
        }
        if (!applicationText.toLowerCase().includes('at') && !applicationText.toLowerCase().includes('place')) {
             missingPoints.push("Specific location or landmarks of the incident.");
        }

        res.json({ 
            success: true, 
            isComplete, 
            missingPoints,
            category: mlResponse.data.prediction.crime_category,
            source: 'Local ML Verification'
        });
    } catch (error) {
        console.error('Local ML Error (Validation):', error.message);
        res.status(500).json({ success: false, message: 'Local ML module failed to verify.' });
    }
};
