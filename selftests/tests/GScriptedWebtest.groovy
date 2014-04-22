// Use scripts at creation time.

// like in ANT, there is a two-phase approach for TaskContainers like 'testSpec':
// first: steps are constructed and stored (creation time)
// second: steps are executed (runtime)

def ant = new AntBuilder()

def webtest_home = System.properties.'webtest.home'

ant.taskdef(resource:'webtest.taskdef'){
//    classpath for taskdef is set from outside for convenience
}

def config_map = [:]
['protocol','host','port','basepath','resultfile',
'resultpath', 'summary', 'saveresponse','defaultpropertytype'].each{
    config_map[it] = System.properties['webtest.'+it]
}

ant.testSpec(name:'groovy: Test Groovy Scripting at creation time'){
    config(config_map)
    steps(){
        invoke(url:'linkpage.html')
        for (i in 1..10){
            verifyText(description:"verify number ${i} is on pages", text:"${i}")
        }
    }
}