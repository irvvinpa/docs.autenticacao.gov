import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import pt.gov.cartaodecidadao.*;


public class GetXML {

    //This static block is needed to load the SDK library
    static {
        try {
            System.loadLibrary("pteidlibj");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
    }
    
    //Main attributes needed for SDK functionalities
    PTEID_ReaderSet readerSet = null;
    PTEID_ReaderContext readerContext = null;
    PTEID_EIDCard eidCard = null;
    PTEID_EId eid = null;

    /**
     * Initializes the SDK and sets main attributes
     * @throws PTEID_Exception when there is some error with the SDK methods
     * @throws Exception when no reader or no card is found/inserted
     */
    public void initiate() throws PTEID_Exception, Exception {
       
        //Must always be called in the beginning of the program
        PTEID_ReaderSet.initSDK();

        //Gets the set of connected readers, if there is any inserted
        readerSet = PTEID_ReaderSet.instance();
        if (readerSet.readerCount() == 0) {
            throw new Exception("No Readers found!");
        }

        //Gets the first reader (index 0) and checks if there is any card inserted
        //When multiple readers are connected, you should iterate through the various indexes
        String readerName = readerSet.getReaderName(0);
        readerContext = readerSet.getReaderByName(readerName);
        if (!readerContext.isCardPresent()) {
            throw new Exception("No card found in the reader!");
        }

        //Gets the card instance
        eidCard = readerContext.getEIDCard();
        eid = eidCard.getID();
    }

    /**
     * Releases the SDK (must always be done at the end of the program)
     */
    public void release() {

        try {
            PTEID_ReaderSet.releaseSDK();

        } catch (PTEID_Exception ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves all card info in XML format and prints it to a file
     * @throws PTEID_Exception when there is some error with the SDK methods
     */
    public void saveXML() throws PTEID_Exception {

        //The number of tries that the user has (updated with each call to verifyPin)
        PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);

        //Sets of the card PINs
        PTEID_Pins pins = eidCard.getPins();
        
        //Gets the specific PIN we want
        //ADDR_PIN - Address Pin
        //AUTH_PIN - Authentication Pin
        //SIGN_PIN - Signature Pin
        PTEID_Pin pin = pins.getPinByPinRef(PTEID_Pin.ADDR_PIN);

        //If the method verifyPin is called with "" as the first argument it prompts the middleware GUI for the user to insert its PIN
        //Otherwise we can send the PIN as the first argument and the end result will be the same
        if (pin.verifyPin("", triesLeft, true)){

            //Selects information to be requested in XML format
            //You can add or remove fields at will
            PTEID_XmlUserRequestedInfo requestedInfo = new PTEID_XmlUserRequestedInfo();

            XMLUserData[] data = new XMLUserData[] { XMLUserData.XML_PHOTO, XMLUserData.XML_NAME, XMLUserData.XML_GIVEN_NAME,
                XMLUserData.XML_SURNAME, XMLUserData.XML_NIC, XMLUserData.XML_EXPIRY_DATE, XMLUserData.XML_GENDER, XMLUserData.XML_HEIGHT,	
                XMLUserData.XML_NATIONALITY, XMLUserData.XML_DATE_OF_BIRTH,	XMLUserData.XML_GIVEN_NAME_FATHER, XMLUserData.XML_SURNAME_FATHER, 
                XMLUserData.XML_GIVEN_NAME_MOTHER, XMLUserData.XML_SURNAME_MOTHER, XMLUserData.XML_ACCIDENTAL_INDICATIONS, XMLUserData.XML_DOCUMENT_NO, 
                XMLUserData.XML_TAX_NO, XMLUserData.XML_SOCIAL_SECURITY_NO, XMLUserData.XML_HEALTH_NO, XMLUserData.XML_MRZ1, XMLUserData.XML_MRZ2, 
                XMLUserData.XML_MRZ3, XMLUserData.XML_CARD_VERSION, XMLUserData.XML_CARD_NUMBER_PAN, XMLUserData.XML_ISSUING_DATE, XMLUserData.XML_ISSUING_ENTITY, 
                XMLUserData.XML_DOCUMENT_TYPE, XMLUserData.XML_LOCAL_OF_REQUEST, XMLUserData.XML_VERSION, XMLUserData.XML_DISTRICT, XMLUserData.XML_MUNICIPALITY,	
                XMLUserData.XML_CIVIL_PARISH, XMLUserData.XML_ABBR_STREET_TYPE,	XMLUserData.XML_STREET_TYPE, XMLUserData.XML_STREET_NAME, XMLUserData.XML_ABBR_BUILDING_TYPE,	
                XMLUserData.XML_BUILDING_TYPE, XMLUserData.XML_DOOR_NO,	XMLUserData.XML_FLOOR, XMLUserData.XML_SIDE, XMLUserData.XML_PLACE,	XMLUserData.XML_LOCALITY, 
                XMLUserData.XML_ZIP4,	XMLUserData.XML_ZIP3, XMLUserData.XML_POSTAL_LOCALITY, XMLUserData.XML_PERSONAL_NOTES,	XMLUserData.XML_FOREIGN_COUNTRY, 
                XMLUserData.XML_FOREIGN_ADDRESS, XMLUserData.XML_FOREIGN_CITY,	XMLUserData.XML_FOREIGN_REGION,	XMLUserData.XML_FOREIGN_LOCALITY,   XMLUserData.XML_FOREIGN_POSTAL_CODE};

            //Adds each field of the vector to the request
            for (XMLUserData field : data) {
                requestedInfo.add(field);
            }

            //Gets the XML information from the card and transforms it to a string
            PTEID_CCXML_Doc ccxml = eidCard.getXmlCCDoc(requestedInfo);
            String resultXml = ccxml.getCCXML();

            try {
                new File("files/info.xml");
                FileWriter writer = new FileWriter("files/info.xml");
                writer.write(resultXml);
                writer.close();
            } catch (IOException e) {
                System.out.println("Couldn't write XML file");
                e.printStackTrace();
            }
        }
    }

    public void start() {

        try {

            initiate();
            saveXML();
            
        } catch (PTEID_Exception ex) {
            Logger.getLogger(GetXML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            release();
        }
    }

    public static void main(String[] args) {
        new GetXML().start();
    }
}
