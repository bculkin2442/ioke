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
        
        loadSpec = method("Load a bundle's info from a .bs file (just ioke code)", bundle_name,
                bundleLocs each(path,
                        "Built path: #{path}/#{bundle_name}.bs" println
                        if(FileSystem file?(path + "/" + bundle_name + ".bs"),
                                return doSpecLoad!(path + "/" + bundle_name + ".bs"),
                                false
                        )
	        )
        )
        
        doSpecLoad! = method("Handle actually loading the BundleSpec", spec_path,
                "Spec_path: " + spec_path println
                
                @bundText = FileSystem readLines(spec_path)
                
                ; Build initial spec
                props = bundText[0] split(";")
                
                spec = BundleSpec mimic(props[0], props[1], props[2])
                
                loaded = false;
                
                ; Handle dependancies
                bundText rest each(depnd,
                        dep = Tuple fromArgs(depnd split(";")
                        
                        if(loadedBunds include?(dep),
                                ; Do nothing, we have an exact match
                                nil,
                                ; Handle a possible inexact match
                                if(loadedBunds select(d, inexactMatch(d, dep)),
                                        ; We got an inexact match
                                        nil,
                                        ; Dispatch a load
                                        
                )
                
                unless(loaded,
                        error!("Could not succesfully load package #{spec first}")
                )
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
