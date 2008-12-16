
DokGen do(
  HtmlGenerator do(
    Templates = Origin mimic
    Templates do(
      Template = Origin mimic
      Template generateIntoFile = method(file, +:krest,
        file print(self data(*krest))
      )

      Readme = Template mimic
      Readme data = Message doText("method(content:, basePath: \"./\", \"#{FileSystem readFully("#{System currentDirectory}/Readme.ik_template")}\")")

      FileFrame = Template mimic
      FileFrame data = Message doText("method(content:, basePath: \"./\", \"#{FileSystem readFully("#{System currentDirectory}/FileFrame.ik_template")}\")")

      KindFrame = Template mimic
      KindFrame data = Message doText("method(content:, basePath: \"./\", \"#{FileSystem readFully("#{System currentDirectory}/KindFrame.ik_template")}\")")

      CellFrame = Template mimic
      CellFrame data = Message doText("method(content:, basePath: \"./\", \"#{FileSystem readFully("#{System currentDirectory}/CellFrame.ik_template")}\")")

      FileFile = Template mimic
      FileFile data = Message doText("method(simpleFileName:, filePath:, fileDate:, methodContent:, macroContent:, syntaxContent:, basePath:, \"#{FileSystem readFully("#{System currentDirectory}/FileFile.ik_template")}\")")

      KindFile = Template mimic
      KindFile data = Message doText("method(kindName:, kindDescription:, inactiveCellsSummary:, activeCellsSummary:, inactiveCellsContent:, activeCellsContent:, allMimics:, mainMimic:, basePath:, \"#{FileSystem readFully("#{System currentDirectory}/KindFile.ik_template")}\")")

      KindFile inactiveCellData = method(cellName:, cellValue:, cellId:,
"        <div class=\"cell\">
          <a name=\"C00#{cellId}\"></a>
          <table border=\"0\" cellpadding=\"5\">
            <tr valign=\"top\">
              <td class=\"cell-name\">#{cellName}</td>
              <td>=</td>
              <td class=\"cell-value\">#{cellValue}</td>
            </tr>
          </table>
        </div>")

      KindFile activeCellData = method(cellName:, cellArguments:, cellDescription:, cellId:,
"        <div class=\"cell\">
          <div class=\"title\">
          <a name=\"C00#{cellId}\"></a><b>#{cellName}</b>(#{cellArguments})
          </div>
          <div class=\"description\">
            <p>
              #{cellDescription}
            </p>
          </div>
          <div class=\"specs\">
            <ul style=\"list-style-type: none;\">
            </ul>
          </div>
        </div>")
    )
  )
)
