Cane addLibraryLocation = method(pth,
	System loadPath append!(pth)
)

Cane loadLibrary = method(libName,
	System loadPath each(path,
		if(FileSystem directory?(path + "/" + libName), doFolderLoad!(path + "/" + libName), false)
	)
)

Cane doFolderLoad! = method(path,
	bind(
		rescue(fn(c, "Package not found println")), 
		use(path + "/init")	
	)
)
