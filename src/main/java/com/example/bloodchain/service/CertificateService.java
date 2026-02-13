package com.example.bloodchain.service;

import com.example.bloodchain.model.BloodUnit;
import com.example.bloodchain.model.User;
import com.example.bloodchain.model.Donor;
import com.example.bloodchain.repository.BloodUnitRepository;
import com.example.bloodchain.repository.UserRepository;
import com.example.bloodchain.repository.DonorRepository;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class CertificateService {

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    public byte[] generateCertificateFromUnit(String bloodUnitId) throws IOException {
        BloodUnit unit = bloodUnitRepository.findByBloodUnitId(bloodUnitId).orElse(null);
        if (unit == null) {
            throw new RuntimeException("Blood Unit not found: " + bloodUnitId);
        }

        // Fetch donor info to get the name
        Donor donor = donorRepository.findById((long) unit.getDonorId());
        String userName = "Valued Donor";
        if (donor != null) {
            User user = userRepository.findByEmail(donor.getEmail());
            if (user != null) {
                userName = user.getName();
            }
        }

        String date = unit.getCollectionDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String location = unit.getStorageLocation() != null ? unit.getStorageLocation() : "Registered Blood Bank";
        
        // Use blockHash if available, otherwise fallback to bloodUnitId for authenticity check
        String hash = unit.getBlockHash() != null ? unit.getBlockHash() : bloodUnitId;

        String htmlContent = generateHtmlTemplate(userName, location, date, unit.getVolumeMl(), hash);

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, target);

        return target.toByteArray();
    }

    private String generateHtmlTemplate(String name, String location, String date, int units, String hash) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "@page { size: A4 landscape; margin: 0; }" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: white; }" +
                ".certificate-container { width: 95%; height: 90%; padding: 30px; border: 15px solid #e53935; background-color: white; margin: 0 auto; position: relative; box-sizing: border-box; }" +
                ".header { text-align: center; margin-bottom: 20px; }" +
                ".logo { color: #e53935; font-size: 36px; font-weight: bold; font-style: italic; }" +
                ".title { font-size: 32px; color: #2c3e50; text-transform: uppercase; letter-spacing: 2px; margin-top: 10px; border-bottom: 2px solid #eee; padding-bottom: 10px; }" +
                ".content { text-align: center; margin-top: 25px; line-height: 1.5; color: #34495e; }" +
                ".donor-name { font-size: 32px; color: #e53935; font-weight: bold; text-decoration: underline; margin: 15px 0; }" +
                ".details { font-size: 18px; margin: 15px 0; }" +
                ".blockchain-info { margin-top: 30px; font-family: monospace; font-size: 11px; color: #7f8c8d; border-top: 1px dashed #ccc; padding-top: 15px; text-align: left; }" +
                ".footer { margin-top: 30px; text-align: center; font-style: italic; color: #95a5a6; font-size: 14px; }" +
                ".seal { position: absolute; bottom: 30px; right: 50px; width: 90px; height: 90px; background: #e53935; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; border: 5px double white; font-weight: bold; transform: rotate(-15deg); font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='certificate-container'>" +
                "    <div class='header'>" +
                "        <div class='logo'>BloodChain</div>" +
                "        <div class='title'>Certificate of Donation</div>" +
                "    </div>" +
                "    <div class='content'>" +
                "        <p>This is to proudly certify that</p>" +
                "        <div class='donor-name'>" + name + "</div>" +
                "        <p class='details'>Participated in a lifesaving blood donation at</p>" +
                "        <p><strong>" + location + "</strong></p>" +
                "        <p>on <strong>" + date + "</strong></p>" +
                "        <p>donating <strong>" + units + " ml</strong> of blood to help those in need.</p>" +
                "    </div>" +
                "    <div class='blockchain-info'>" +
                "        <p><strong>Blockchain Verification Hash:</strong> " + hash + "</p>" +
                "        <p>Stored securely on the BloodChain distributed ledger.</p>" +
                "    </div>" +
                "    <div class='footer'>" +
                "        <p>Thank you for your selfless contribution. You are a hero!</p>" +
                "    </div>" +
                "    <div class='seal'>VERIFIED</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
