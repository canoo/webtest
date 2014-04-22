// Example code that shows how to reuse WebTest Steps in a groovy Step.

def ant = new com.canoo.webtest.extension.groovy.MacroStepBuilder(step)

// ---------------------------------
// use webtest steps like in a macro
// ---------------------------------

// our running example: use the invoke step
ant.invoke(url:"trafficlight.html")

// we do not cheat: the properties come from outside
def imageId  = step.project.properties.image_id
def imageSrc = step.webtestProperties.imagesrc;

// use the source, luke...
ant.storeXPath(
    description : "extract src attribute from image with id=${imageId}",
    xpath       : "//img[@id='${imageId}']/@src",
    property    : "imagesrc")

// same as in the inline script
def src2alt = ['red.gif':'stop', 'orange.gif':'wait', 'green.gif':'go']

// use the WebTest Step for verification and reporting
ant.verifyXPath(
    description : "check alt value",
    xpath       : "//img[@id='${imageId}']/@alt",
    text        : src2alt[imageSrc] )

ant.not(description: 'use container'){
    verifyText(text: 'not on page')
}

// ----------------------------------
// same as above but more script-like
// ----------------------------------
ant.invoke(url: "trafficlight.html")
def page     = step.context.currentResponse
def img = page.getHtmlElementById("${imageId}")
def imgSrc   = img.srcAttribute
def imgAlt   = img.altAttribute
src2alt      = ['red.gif':'stop', 'orange.gif':'wait', 'green.gif':'go']
if (src2alt[imgSrc] != imgAlt){
    throw new com.canoo.webtest.engine.StepFailedException(
        "failed with src ${imgSrc} and alt ${imgAlt}", step
    )
}
