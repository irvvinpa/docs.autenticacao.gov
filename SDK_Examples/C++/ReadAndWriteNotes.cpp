#include "eidlib.h"
#include <iostream>

using namespace eIDMW;

int main(int argc, char **argv) {

    //Initializes SDK (must always be called in the beginning of the program)
    eIDMW::PTEID_InitSDK();

    //As the name indicates, gets the number of connected readers, exits the program if no readers were found
    //Also checks if there is a card present, if not exits the program
    if (PTEID_ReaderSet::instance().readerCount() == 0) 
    {
		std::cout << "No readers found!" << std::endl;
    }
    else if (!PTEID_ReaderSet::instance().getReader().isCardPresent())
	{
		std::cout << "No card found in the reader!" << std::endl;
	}
    else 
    {
        //Gets the set of readers connected to the system
        PTEID_ReaderSet& readerSet = PTEID_ReaderSet::instance();
        
        //Gets a reader connected to the system (useful if you only have one)
        //Alternatively you can iterate through the readers using getReaderByNum(int index) instead of getReader()
        PTEID_ReaderContext& readerContext = PTEID_ReaderSet::instance().getReader();
        
        //Gets the EIDCard and EId objects (with the cards information)
        PTEID_EIDCard& eidCard = PTEID_ReaderSet::instance().getReader().getEIDCard();
        PTEID_EId& eid = PTEID_ReaderSet::instance().getReader().getEIDCard().getID();

        //Read current notes and print them
        const char *my_notes = eidCard.readPersonalNotes(); 
        std::cout << "Current notes: " << my_notes << std::endl;

        //Prepares a string (notes) to be written in the card
        std::string notes("We wrote successfully to the card!");
        PTEID_ByteArray personalNotes((const unsigned char*) notes.c_str(), notes.size() + 1);
        bool ok;

        //Gets authentication pin
        PTEID_Pins& pins = eidCard.getPins();
        PTEID_Pin& pin = pins.getPinByPinRef(PTEID_Pin::AUTH_PIN);

        //Writes notes to card
        ok = eidCard.writePersonalNotes(personalNotes, &pin); 

        std::cout << "Was writing successful? " << (ok ? "Yes!" : "No.") << std::endl;
    }

    //Releases SDK (must always be called at the end of the program)
    eIDMW::PTEID_ReleaseSDK();
}