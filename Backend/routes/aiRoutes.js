const express = require('express');
const { suggestActs, compareFir, chat } = require('../controllers/aiController');
const { protect } = require('../middlewares/authMiddleware');

const router = express.Router();

// Protected routes requiring standard authentication
router.post('/suggest-acts', protect, suggestActs);
router.post('/verify-application', protect, require('../controllers/aiController').verifyApplication);
router.post('/draft-fir/:applicationId', protect, require('../controllers/aiController').draftFir);
router.get('/compare-fir/:applicationId', protect, compareFir);
router.post('/chat', protect, chat);

module.exports = router;
