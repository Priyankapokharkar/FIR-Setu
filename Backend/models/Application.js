const mongoose = require('mongoose');

const documentSchema = new mongoose.Schema({
    filename: String,
    url: String,
    mimetype: String
}, { _id: false });

const ApplicationSchema = new mongoose.Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    description: {
        type: String,
        required: true
    },
    suggestedActs: [{
        actName: String,
        description: String
    }],
    status: {
        type: String,
        enum: ['Pending', 'Approved', 'Rejected'],
        default: 'Pending'
    },
    assignedConstable: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User'
    },
    rejectionReason: {
        type: String,
        default: null
    },
    // Supporting documents if any uploaded during the application process or chat
    documents: [documentSchema],
    // The FIR linked if approved
    linkedFir: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Fir'
    },
    // URL/Path to the formal FIR PDF
    generatedFirUrl: {
        type: String,
        default: null
    },
    // Flag to indicate if the FIR was uploaded manually (Offline mode)
    isOfflineFir: {
        type: Boolean,
        default: false
    }
}, { timestamps: true });

module.exports = mongoose.model('Application', ApplicationSchema);
