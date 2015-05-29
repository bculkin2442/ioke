; A third attempt at Cane due to poor execution planning of the last
; The intended usage of cane is as follows
;       use(...) ; Load cane itself
;       caneLoad("bund-name") ; Load a bundle with the default cane loader, raising an error on failure
;       xxx xxx = bund-name doStuff ; Use your newly loaded bundle

Cane = Origin mimic do(
        initialize = method(bundle_location "lib/ioke/bundles/",
                @locations = list(bundle_location)
                locations documentation = " A list of places to find bundles in."
                
                @bundles = list()
                bundles documentation = " A list of all currently loaded bundles."
                
                ; Load the initial bundles
                bundles concat!( list(tuple("Ioke", 0, 4), tuple("Cane", 0, 1)) )
        )
        
        versionMatch = method("Find a version compatible with the specified one in a list", desired, versions, strict false,
                ; Version format is string patterned as major.minor. Strict means that both major and minor must match, otherwise
                ;       only major must match; minor simply needs to be greater than or equal to the desired one.
                ; NOTE: A later version is always preferred in case there are multiple valid versions
                tuple("dummy to mark arg end")
                
                major, minor = desired split(".")
                versions each(split(".") asTuple) select(ver,
                        if(ver first == major,
                                if(strict,
                                        ver second == minor,
                                        ver second >= minor
                                ),
                                false
                        )
                ) select last
        )
        
        load = method("Load a bundle and all of its dependancies", bundle_name, version "latest", strict false,
                path = locations each(location,
                        "#{location}#{bundle_name}.bs" println
                        if(FileSystem file?("#{location}#{bundle_name}.bs"),
                                "#{location}#{bundle_name}.bs",
                                nil
                        )
                )
                
                path inspect println
                
                ; Handle not finding any version of the package at all
                unless(path,
                        error!("Didn't find valid .bs file for bundle #{bundle_name}")
                )
                
                spec = FileSystem readLines(path)
                
                ; Make sure that the latest version is last
                
                spec sort!
                
                ; We need to figure out which version to load
                version = if(spec empty?,
                        error!("No valid versions detected for bundle #{bundle_name}"),
                        unless(version == "latest",
                                versionMatch(version, spec, strict),
                                spec last
                        )
                )
                
                vText = version first + "." + version second + "/" + bundle_name + ".bs"

                path = path replace(".bs", "/") + vText
                spec = FileSystem readLines(path)
                
                ; Load dependancies, reporting errors in their loading. The first error stops all loading
                status = spec select(ln, #/^#/ !~ ln) each(dep, ; Filter out comments
                        tmp = dep split(" ") asTuple
                        ; TODO figure out a way to note whether a dependancy is strict or "pinned" to a particular version
                        bind(
                             rescue(fn(c,
                                "#{c}" println
                                error!("Error loading dependency #{tmp first} for bundle #{bundle_name}.")
                             )),
                             load(tmp first, tmp second)
                                
                        )
                )
                
                if(status include?(false),
                        error!("Could not succesfully load dependancies for #{bundle_name}")
                )
                
                ; Actually load the bundle itself
                
                path = path replace("#{bundle_name}.bs", "init.bs")
                
                use(path)
        )
)

internal:pkgMan = Cane mimic

caneLoad = method("Load a bundle from the default bundle loader", bund_name,
        internal:pkgMan load(bund_name)
)
