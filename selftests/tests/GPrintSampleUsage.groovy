// some code that shows what you can access in a Groovy step

// explicitly passed variables into the Binding are:
println step

// this is the first level of accessing:

print 'methods of step:'
print group(step.class.methods.name)

print 'properties of step:'
print group(step.properties.keySet() )

// most of the useful information is in the context

print 'methods of step.context:'
print group(step.context.class.methods.name)

// print information about names in the given array of objects that have 'name' property
// sorted alphabetically and grouped by first character
def group(names){
    def result = ''
    names = names.sort()
    def lastIndexChar = 'z'
    names.each{ name ->
        if (name[0]!=lastIndexChar){
            result += "\n\t"
            lastIndexChar = name[0]
        }
        result += " ${name}"
    }
    result += "\n---\n"
}