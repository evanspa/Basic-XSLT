# BasicXSLT

<img src="https://github.com/downloads/evanspa/BasicXSLT/BasicXSLT-logo.jpg"
 alt="BasicXSLT logo" align="right" />

BasicXLST is a Java-based graphical tool for applying a chain of XSL transformations  
to an XML document.

# History and Current State

This code was created in April of 2005 and is no longer maintained.

# Project Details

Basic XSLT is a Java-based application used for transforming an XML file using a chain of XSL stylsheets. It features a Swing-based GUI and includes capabilities useful to developers when creating XSL stylesheets. It also produces timing information, which is valuable during tuning exercises. Development on Basic XSLT began in early 2003 and it has been used on various projects since then. Currently, Basic XSLT uses the Apache Xerces and Xalan APIs for parsing and performing XSL transformations.

Basic XSLT allows the XSL developer to specify a chain of stylesheets for transforming an XML file. Of course, the user may wish to specify a single XSL stylesheet, which is fine. For each XSL stylesheet, the user can setup parameters and output properties. One of the goals of Basic XSL is to expose using the GUI most, if not all, of the features available with an XSL transformer.

Basic XSLT is a developer-centric tool designed to aid and save time for the XSL developer. One such time-saving feature is the ability for Basic XSLT to remember its state upon startup from the previous time it was used. Everything is remembered; from the window size and screen location, to the XML and XSL files being used, as well as parameters and output properties specified for each stylesheet. Suppose you had setup the tool to transform some XML file on the file system using a chain of 5 different XSL stylesheets with each one setup with a collection of parameters and output properties. It may have taken several minutes to create this "configuration." When the tool is shutdown, all of the information regarding the tools' state is persisted so that when the tool is launched again, it is initialized with all of the previous entries, enabling the developer to focus on the the task at hand rather than having to spend several minutes trying to remember what parameters went with what stylesheets and where the files were sitting on the file system, etc. Basic XSLT allows the user to create multiple configurations as well as load them at any time.

Our goal is to see Basic XSLT grow to incorporate more developer-friendly features. One such feature would be to specify different transformers at run time so as to compare the timings of different transformer implementations. Please use the project page to specify improvements. Basic XSLT was developed using Java 5.0.
