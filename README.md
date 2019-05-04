# StudentIDCardScanner

### The application's pipeline is as follows:
1. Choose a photo of the card (The photo is acquired either using the mobile's built-in camera or simply chosen from gallery)
2. Process this photo by applying several image processing techniques to "scan" it
3. Run a face detector classifier to detect the face on the card 
4. Run an OCR to detect the student's code on the card 
5. Inspect the card by searching for the recognized student code present on the card in the JSON file containing students information (([parseJSON.java](app/src/main/java/com/example/cardscanner/parseJSON.java)) --> [JSON files](/app/src/main/assets/JSON/))

### Implementation details:
- A mobile app that runs on Android, the minimum Android version supported is **Lollipop 5.0** (API **21**) 
  - [Java files](app/src/main/java/com/example/cardscanner/)
  - [XML layout files](app/src/main/res/layout/)
- OpenCV in Java for implementing the image processing methods in the ([ImgProcPipeline.java](app/src/main/java/com/example/cardscanner/ImgProcPipeline.java))
- tess-two (Tesseract Android Library) for OCR ([tessOCR.java](app/src/main/java/com/example/cardscanner/tessOCR.java))


### Team members:
- Bothayna Hazem (**43754**) 
- Ziad Gamal (**43786**)
- Ziad Hesham (**43787**)
- Marina Saad (**43850**)
- Mohamed Abdelrahim (**43869**)
