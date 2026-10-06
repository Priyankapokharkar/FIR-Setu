const Application = require('../models/Application');
const Fir = require('../models/Fir');

// @route   GET /api/constable/applications
// @desc    Get pending applications (could filter by region if mapped, but getting all for now)
// @access  Private (Constable only)
exports.getPendingApplications = async (req, res) => {
    try {
        const applications = await Application.find({ status: 'Pending' })
            .populate('user', 'name email location')
            .populate('assignedConstable', 'name badgeNumber')
            .sort({ createdAt: -1 });

        res.json({ success: true, applications });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   PUT /api/constable/application/:id/status
// @desc    Approve or Reject an application
// @access  Private (Constable only)
exports.updateApplicationStatus = async (req, res) => {
    try {
        const { status, rejectionReason, firText, appliedActs } = req.body;
        
        const application = await Application.findById(req.params.id);
        if (!application) {
            return res.status(404).json({ success: false, message: 'Application not found' });
        }

        application.status = status;
        application.assignedConstable = req.user._id;

        if (status === 'Rejected') {
            if (!rejectionReason) return res.status(400).json({ success: false, message: 'Rejection reason is required' });
            application.rejectionReason = rejectionReason;
            await application.save();
        } else if (status === 'Approved') {
            if (!firText) return res.status(400).json({ success: false, message: 'FIR text is required to approve' });
            
            // Generate the actual FIR
            const fir = await Fir.create({
                applicationId: application._id,
                user: application.user,
                constable: req.user._id,
                firText,
                appliedActs: appliedActs || []
            });

            application.linkedFir = fir._id;
            await application.save();
        }

        res.json({ success: true, application });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   POST /api/constable/direct-fir
// @desc    File a direct FIR targeting a user account
// @access  Private (Constable only)
exports.createDirectFir = async (req, res) => {
    try {
        const { targetUserId, firText, appliedActs } = req.body;

        // 1. Create the base FIR first
        const fir = await Fir.create({
            user: targetUserId,
            constable: req.user._id,
            firText,
            appliedActs: appliedActs || [],
            isDirectFir: true
        });

        // 2. Automatically create a linked Approved Application so it shows in the User's Track Status
        const application = await Application.create({
            user: targetUserId,
            description: `[DIRECT FIR] ${firText}`,
            status: 'Approved',
            assignedConstable: req.user._id,
            linkedFir: fir._id,
            isOfflineFir: false
        });

        // 3. Link the FIR back to the new Application for circular tracking
        fir.applicationId = application._id;
        await fir.save();

        res.status(201).json({ 
            success: true, 
            fir, 
            applicationId: application._id,
            message: 'Direct FIR filed and linked to user track status.' 
        });
    } catch (error) {
        console.error('Direct FIR Error:', error.message);
        res.status(500).json({ success: false, message: error.message });
    }
};

// @route   GET /api/constable/history
// @desc    Get applications processed by the logged-in constable
// @access  Private (Constable only)
exports.getApprovedHistory = async (req, res) => {
    try {
        const applications = await Application.find({ 
            assignedConstable: req.user._id,
            status: { $in: ['Approved', 'Rejected'] }
        })
        .populate('user', 'name email location')
        .populate('assignedConstable', 'name badgeNumber')
        .populate('linkedFir')
        .sort({ updatedAt: -1 });

        res.json({ success: true, applications });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
};
