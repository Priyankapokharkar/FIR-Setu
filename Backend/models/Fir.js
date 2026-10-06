const mongoose = require('mongoose');

const FirSchema = new mongoose.Schema({
    applicationId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Application',
        default: null // Null if it's a direct FIR
    },
    user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true // The person who reported
    },
    constable: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true // The constable who filed this
    },
    firText: {
        type: String,
        required: true
    },
    isDirectFir: {
        type: Boolean,
        default: false
    },
    // The acts that the constable actually applied
    appliedActs: [{
        actName: String,
        description: String
    }],
    aiComparisonReport: {
        // We will store the AI comparison report here if user requests it
        type: String,
        default: null
    }
}, { timestamps: true });

module.exports = mongoose.model('Fir', FirSchema);
