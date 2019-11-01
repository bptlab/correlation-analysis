    <section class="section">
        <div class="container">
            <form action="${contextPath}/upload" method="post" enctype="multipart/form-data">

                <div class="field">
                  <label class="label">Project Name</label>
                  <div class="control">
                    <input class="input" type="text" name="projectName" placeholder="Project Name">
                  </div>
                </div>

                <div class="field">
                    <label class="label">Target Attribute</label>
                    <div class="control">
                        <input class="input" type="text" name="targetAttribute" placeholder="Target Attribute">
                    </div>
                </div>

                <div class="field">
                    <label class="label">Correlation Assumptions</label>
                    <div class="control">
                        <input class="input" type="text" name="suspectedDependencies"
                               placeholder="Correlation Assumptions">
                    </div>
                </div>

                <div id="file-upload" class="field file">
                  <label class="file-label">
                    <input class="file-input" type="file" name="caseLog">
                    <span class="file-cta">
                      <span class="file-label">
                        Choose a file…
                      </span>
                    </span>
                    <span class="file-name">
                      No file uploaded
                    </span>
                  </label>
                </div>

                <div class="field">
                  <div class="control">
                      <input type="submit" value="Upload" class="button is-primary">
                  </div>
                </div>
            </form>
        </div>
    </section>


    <script>
      const fileInput = document.querySelector('#file-upload input[type=file]');
      fileInput.onchange = () => {
        if (fileInput.files.length > 0) {
          const fileName = document.querySelector('#file-upload .file-name');
          fileName.textContent = fileInput.files[0].name;
        }
      }
    </script>