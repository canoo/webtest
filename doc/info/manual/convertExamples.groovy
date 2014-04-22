/*
 * This script is to be called from ant's groovy task.
 * Parameter:
 * ant                       - the AntBuilder, set by the groovy task
 * properties['dir.name']    - the subdirectory with examples to convert
 * properties['generated.dir']    - the destination directory for converted examples
 */

exampleCategory = properties['dir.name']
srcDir = new File(exampleCategory)
destDir = new File(properties['generated.dir'], exampleCategory)

scanner = ant.fileScanner {
	fileset(dir:srcDir) {
		include(name:'**/*.txt')
	}
}

replacements = [                       // sequence of patterns and and their replacement expressions
    [ ~/&(\w+);/       , '&amp;$1;' ], // xmlEntities
    [ ~/<!--/          , '&lt;--'   ], // commentStart
    [ ~/<%/            , '&lt;%'    ], // jspSyntax
    [ ~/\{\{\{<\}\}\}/ , '&lt;'     ], // jspLessThanHack
    [ ~/>/             , '&gt;'     ], // greaterThan
    [ ~/<!\[/          , '&lt;!\\[' ], // cdata
                                       // changes from perl version here:
                                       // perl only converted the leading tabs,
                                       // groovy removes trailing tabs and convert all the remaining ones.
    [ ~/\t+$/          , ''         ], // trailingTabulators
    [ ~/\t/            , '<t/>'     ], // tabulator
                                       // go for tags that are at least 2 chars long to omit the <t>
                                       // this handle plain tag name, entity declaration and processing instructions.
    [ ~/<([!?]?[\p{Alpha}\p{Digit}:]{2,})\b/   , '&lt;<tag>$1</tag>'         ], // openTag
    [ ~/<p\b/                                  , '&lt;<tag>p</tag>'          ], // openPTag
    [ ~/\b([:\w]+)\s*=\s*"(.*?)"/                 , '<att>$1</att><val>$2</val>'], // attributeValue
    [ ~/<< /                                   , '&lt;&lt; '                 ], // compareToExpected error
    [ ~/>> /                                   , '&gt;&gt; '                 ], // compareToExpected error
    [ ~/< /                                    , '&lt; '                     ], // remainingLessThan
    [ ~/ /                                     , '&nbsp;'                    ], // blank
    [ ~/<\/([:\w]+)&gt;/                          , '&lt;/<tag>$1</tag>&gt;'    ]  // closingTag
]

for (f in scanner) {
	destFile = new File(destDir, f.name - '.txt' + '.xml')
	if (destFile.lastModified() > f.lastModified()) continue

	destFile.withWriter { out ->
		out = new PrintWriter(out)
		out.println '<?xml version="1.0" ?>'
		out.println '<!DOCTYPE source [<!ENTITY nbsp "&#160;">]>'
		out.println "<source>"

		f.eachLine { line ->
		    replacements.each { pair ->
		        line = pair[0].matcher(line).replaceAll(pair[1])
		    }
			line += '<n/>'
			out.println line
		}
		out.println '</source>'
	}
}
