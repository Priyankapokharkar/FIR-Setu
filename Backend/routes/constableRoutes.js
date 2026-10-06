const express = require('express');
const { 
    getPendingApplications, 
    updateApplicationStatus, 
    createDirectFir,
    getApprovedHistory 
} = require('../controllers/constableController');
const { protect, authorize } = require('../middlewares/authMiddleware');

const router = express.Router();

router.use(protect);
router.use(authorize('constable')); // Only constables can access these routes

router.get('/applications', getPendingApplications);
router.get('/history', getApprovedHistory);
router.put('/application/:id/status', updateApplicationStatus);
router.post('/direct-fir', createDirectFir);

module.exports = router;
