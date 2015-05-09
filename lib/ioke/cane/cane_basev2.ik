; OLD Cane code. This'll get swept out when I'm sure I don't need it
;Cane addLibraryLocation = method(pth,
;	System loadPath append!(pth)
;)
;
;Cane loadLibrary = method(libName,
;	System loadPath each(path,
;		if(FileSystem directory?(path + "/" + libName), doFolderLoad!(path + "/" + libName), false)
;	)
;)
;
;Cane doFolderLoad! = method(path,
;	bind(
;		rescue(fn(c, "Package not found println")), 
;		use(path + "/init")	
;	)
;)

Cane = Origin mimic

Cane BundleDB = Origin mimic do(
        documentation = "Database for holding loaded bundles."
        
        BundleSpec = Origin mimic do(
                documentation = "Specification for a single, installed bundle."

                name = "Unnamed Bundle"
                major_version = 0
                minor_version = 0
                depends = list()
                
                name documentation = "The name of the bundle."
                
                major_version documentation = "The major version of this bundle. Every major version is assumed to be mutually incompatible."
                minor_version documentation = "The minor version of this bundle. Every minor version is assumed to be backwards compatible with all major versions before it."
                
                depends documentation = "A list of all of this packages dependancies. Is a list of (name, major, minor) tuples."
                
                initialize = method("Create a new bundle with set name, major/minor version and dependancies.",
                        bundle_name, majr_version 1, minr_version 0, dependancies list(),
                                @name = bundle_name
                                @major_version = majr_version
                                @minor_version = minr_version
                                @depends = dependancies
                )
                
                print = method(
                        printDepend = method("Print out info for a dependancy", dependancy, indent,
                                indent + "Depends on #{dependancy first} v#{dependancy second}.#{dependancy third}."
                        )
                        
                        "Bundle Details for bundle #{name} v#{major_version}.#{minor_version}: " println
                        depends each(dependency
                                printDepend(dependancy, "\t")
                        )
                )   
        )

        initialize = method(bundLocs,
                @loadedBunds = list(
                        tuple("Ioke", 0, 4),
                        tuple("Cane", 0, 1),
                        tuple("BundleDB", 0, 1)
                )
                
                @loadedBunds documentation = "A list of all currently loaded bundles."
                
                @bundleLocs = bundLocs
                
                @bundleLocs documentation = "A list of places to load bundles from."
        )
        
        listLoaded = method("List all currently loaded bundles",
                "Currently loaded bundles" println
                loadedBunds each(bund,
                        "Bundle #{bund first} v#{bund second}.#{bund third}" println
                )
                
                nil; Don't print out the list of loaded bundles again
        )
        
        toDependTuple = method("Convert a BundleSpec to a dependancy tuple.", bund_spec,
                tuple(bund_spec name, bund_spec major_version, bund_spec minor_version)
        )
        
        loadSpec = method("Load a BundleSpec from a .bs file (just ioke code)", bundle_name,
                bundleLocs each(path,
                        "Built path: " + path + bundle_name + ".bs" println
                        if(FileSystem file?(path + "/" + bundle_name + ".bs"),
                                doSpecLoad!(path + "/" + bundle_name + ".bs"),
                                false
                        )
	        )
        )
        
        doSpecLoad! = method("Handle actually loading the BundleSpec", spec_path,
                
                bundSpec = do(FileSystem readFully(spec_path))
                
                bundSpec print
        )
)

Cane initialize = method(
        bundleLoc = getSysProperty("ioke.cane.pkgloc")
        
        @bundleLocs = if(bundleLoc, bundleLoc split(","), list("lib/ioke/bundles"))
        
        @db = BundleDB mimic(bundleLocs)
)

Cane load = method("Load a package and its dependancies.", bundle_name,

)

; Non-cane utility code @TODO Perhaps move this into some other file elsewhere?
getSysProperty = method( "Get a property out of the java system properties as a string.", prop_name,
        java:lang:System getProperty(prop_name)
)

setSysProperty = method("Write a property to the java system properties.", prop_name, prop_value,
        java:lang:System setProperty(prop_name, prop_value)
)
