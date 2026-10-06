const express = require('express');
const multer = require('multer');
const path = require('path');
const { 
    submitApplication, 
    getMyApplications, 
    getApplicationDetails, 
    listCitizens,
    generateFirPdf,
    uploadOfflineFir
} = require('../controllers/userController');
const { protect, authorize } = require('../middlewares/authMiddleware');

const router = express.Router();

// Multer Config for FIR Uploads
const storage = multer.diskStorage({
    destination: (req, file, cb) => cb(null, 'uploads/'),
    filename: (req, file, cb) => cb(null, `MANUAL_FIR_${Date.now()}${path.extname(file.originalname)}`)
});
const upload = multer({ storage });

// Publicly accessible PDF viewing (for browser intents)
router.get('/view-pdf/:filename', require('../controllers/userController').viewPdf);

router.use(protect);

// Constable only routes
router.get('/list', authorize('constable'), listCitizens);
router.post('/upload-offline-fir/:id', authorize('constable', 'user'), upload.single('fir'), uploadOfflineFir);

// Shared Constable/User routes
router.post('/generate-fir-pdf/:id', generateFirPdf);

// Citizen only routes
router.post('/application', authorize('user'), submitApplication);
router.get('/applications', authorize('user'), getMyApplications);
router.get('/application/:id', authorize('user'), getApplicationDetails);

module.exports = router;
