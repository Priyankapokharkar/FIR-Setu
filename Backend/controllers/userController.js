const Application = require('../models/Application');
const User = require('../models/User');
const pdfUtil = require('../utils/pdfUtil');
const path = require('path');
const fs = require('fs');
const pdfParse = require('pdf-parse');

// @route   POST /api/user/application
// @desc    Submit a new FIR application
// @access  Private (User only)
exports.submitApplication = async (req, res) => {
    try {
        const { description, suggestedActs } = req.body;

        const application = await Application.create({
            user: req.user._id,
            description,
            suggestedActs: suggestedActs || [],
            status: 'Pending'
        });

        res.status(201).json({ success: true, application });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   GET /api/user/applications
// @desc    Get all applications submitted by the logged-in user
// @access  Private (User only)
exports.getMyApplications = async (req, res) => {
    try {
        const applications = await Application.find({ user: req.user._id })
            .populate('assignedConstable', 'name badgeNumber')
            .populate('linkedFir')
            .sort({ createdAt: -1 });

        res.json({ success: true, applications });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   GET /api/user/application/:id
// @desc    Get details of a specific application
// @access  Private (User only)
exports.getApplicationDetails = async (req, res) => {
    try {
        const application = await Application.findOne({ _id: req.params.id, user: req.user._id })
            .populate('assignedConstable', 'name badgeNumber')
            .populate('linkedFir');

        if (!application) {
            return res.status(404).json({ success: false, message: 'Application not found' });
        }

        res.json({ success: true, application });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   GET /api/user/list
// @desc    Get all registered citizens (Constable use only)
// @access  Private (Constable)
exports.listCitizens = async (req, res) => {
    try {
        const citizens = await User.find({ role: 'user' })
            .select('name email _id')
            .sort({ name: 1 });

        res.json({ success: true, citizens });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   POST /api/user/generate-fir-pdf/:id
// @desc    Generate formal FIR PDF for an application
// @access  Private (Constable/User)
exports.generateFirPdf = async (req, res) => {
    try {
        const application = await Application.findById(req.params.id).populate('user', 'name email');
        if (!application) {
            return res.status(404).json({ success: false, message: 'Application not found' });
        }

        const fileName = `FIR_${application._id}.pdf`;
        const filePath = path.join(__dirname, '../uploads', fileName);

        await pdfUtil.generateFIR(application, filePath);

        // Generate a cleaner URL using the new view-pdf route
        const fullUrl = `${req.protocol}://${req.get('host')}/api/user/view-pdf/${fileName}`;
        application.generatedFirUrl = fullUrl;
        application.isOfflineFir = false;
        await application.save();

        res.json({ 
            success: true, 
            message: 'FIR PDF generated successfully',
            fileUrl: fullUrl 
        });
    } catch (error) {
        console.error('PDF Generation Error:', error);
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   POST /api/user/upload-offline-fir/:id
// @desc    Upload manually drafted FIR PDF (Offline Mode)
// @access  Private (Constable Only)
exports.uploadOfflineFir = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ success: false, message: 'No PDF file uploaded' });
        }

        const application = await Application.findById(req.params.id);
        if (!application) {
            return res.status(404).json({ success: false, message: 'Application not found' });
        }

        const filePath = req.file.path;
    

        // Extract text from the uploaded PDF
        const dataBuffer = fs.readFileSync(filePath);
        const data = await pdfParse(dataBuffer);
        const extractedText = data.text;

        if (!extractedText || extractedText.trim().length === 0) {
            return res.status(400).json({ 
                success: false, 
                message: 'This PDF seems to be a scan or image. Please use a text-based PDF for AI verification.' 
            });
        }

        // Generate AI comparison report for the offline FIR
        const aiController = require('./aiController');
        const report = await aiController.generateComparisonReport(
            application.description,
            extractedText,
            "N/A (Offline Manual FIR)",
            application.suggestedActs,
            []
        );

        const fullUrl = `${req.protocol}://${req.get('host')}/api/user/view-pdf/${req.file.filename}`;
        application.generatedFirUrl = fullUrl;
        application.isOfflineFir = true;
        application.status = 'Approved';
        // We can store the report in the application or just return it
        // For consistency with compareFir, let's return it
        await application.save();

        res.json({ 
            success: true, 
            message: 'Manual FIR uploaded and analyzed successfully',
            fileUrl: fullUrl,
            report: report 
        });
    } catch (error) {
        console.error('Offline FIR Processing Error:', error);
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   GET /api/user/view-pdf/:filename
// @desc    Securely stream PDF to mobile browser
// @access  Public (Allow browser intents)
exports.viewPdf = async (req, res) => {
    try {
        const fileName = req.params.filename;
        const filePath = path.join(__dirname, '../uploads', fileName);

        if (!fs.existsSync(filePath)) {
            return res.status(404).send('PDF not found on server');
        }

        res.set({
            'Content-Type': 'application/pdf',
            'Content-Disposition': 'inline; filename="' + fileName + '"',
            'Cache-Control': 'no-cache'
        });

        const fileStream = fs.createReadStream(filePath);
        fileStream.pipe(res);
    } catch (error) {
        res.status(500).send('Error reading PDF');
    }
};
