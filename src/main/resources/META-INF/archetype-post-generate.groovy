import groovy.io.FileType


// Make scripts executable

def scriptFolder = new File(request.getOutputDirectory()+"/"+request.getArtifactId()+"/bin")

scriptFolder.eachFile (FileType.FILES) { file ->
  file.setExecutable(true, false)
}

