# AlfaFéra
A régebbi DeltaFérá-nak egy jobb változata

### Jelenlegi featureök
 - Jegyzetek automatikus hozzáadása az eredeti weboldalról
 - Hasonló jegyzetek mutatása k-NN algoritmussal
 - Jegyzetek szűrése / keresése
 - Pdf nézegető telefonra
 - Jegyzetek képeinek automatikus kirenderelése

### Követelmények ha a kódon szeretnél változtatni:
 - A kód legyen [Oracle](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html) stílusban formázva
 - Ne legyenek random sortörések, kivéve ha: chain-eket kezelsz, vagy sok argument van
 - Minden legyen szépen dokumentálva: Classok, Methodok kapjanak javadoc-ot, hosszabb methodok belül is legyenek dokumentálva
 - Próbálj a gyorsaságra törekedni
 
### Tervezett dolgok amiket hozzá kéne adni:
 - Async betöltés: gyorsabb
 - Jegyzetek a memóriában, és sosem kerülnek file-ba
 - Szebb HTTP error oldalak
 - Final szó bespammelése mindenhova ahova csak lehet
 - Logok elérése
 - Github linkelése
 - Működő release

### Felhasznált projektek
[Pdf.js](https://mozilla.github.io/pdf.js/)
[Jsoup](https://jsoup.org)
[Apache Pdfbox](https://pdfbox.apache.org)
[Swing PDF Renderer](https://mvnrepository.com/artifact/org.swinglabs/pdf-renderer)
[Javalin](https://javalin.io)
