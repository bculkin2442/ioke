use("ispec")

describe(Cane,
	describe("addLibraryLocation",
		it("should add to the load path",
			Cane addLibraryLocation("test")
			System loadPath include?("test") should == true
		)
	)
)
