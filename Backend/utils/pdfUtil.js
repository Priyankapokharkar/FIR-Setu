const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');

/**
 * Generates a formal FIR PDF for an application.
 * @param {Object} application - The application object with user and suggestions.
 * @param {String} outputPath - Local file path to save the PDF.
 */
exports.generateFIR = (application, outputPath) => {
    return new Promise((resolve, reject) => {
        const doc = new PDFDocument({ margin: 50 });

        // Pipe its output somewhere, e.g. onto the disk
        const stream = fs.createWriteStream(outputPath);
        doc.pipe(stream);

        // Header
        doc.fontSize(20).text('OFFICIAL FIRST INFORMATION REPORT (FIR)', { align: 'center' });
        doc.moveDown();
        doc.fontSize(12).text(`Generated Date: ${new String(new Date().toLocaleString())}`, { align: 'right' });
        doc.moveDown();

        doc.moveTo(50, doc.y).lineTo(550, doc.y).stroke();
        doc.moveDown();

        // Application Details
        doc.fontSize(14).text('Application Details:', { underline: true });
        doc.fontSize(12).text(`Application ID: ${application._id}`);
        doc.text(`Status: ${application.status}`);
        doc.text(`Incident Reported By: ${application.user.name} (${application.user.email})`);
        doc.moveDown();

        // Incident Description
        doc.fontSize(14).text('Incident Description:', { underline: true });
        doc.fontSize(11).text(application.description, { align: 'justify' });
        doc.moveDown();

        // Legal Acts
        if (application.suggestedActs && application.suggestedActs.length > 0) {
            doc.fontSize(14).text('Applicable Legal Sections:', { underline: true });
            application.suggestedActs.forEach((act, index) => {
                doc.fontSize(11).text(`${index + 1}. ${act.actName}:`, { indent: 20 });
                doc.fontSize(10).text(act.description, { indent: 40 });
                doc.moveDown(0.5);
            });
        }

        doc.moveDown(2);
        doc.fontSize(10).text('This is a computer-generated document authorized by the Ministry of Law and Justice.', { align: 'center', oblique: true });
        doc.text('Valid only with official digital signature.', { align: 'center', oblique: true });

        // Finalize the PDF and end the stream
        doc.end();

        stream.on('finish', () => {
            resolve(outputPath);
        });

        stream.on('error', (err) => {
            reject(err);
        });
    });
};
