<section class="section">
<div class="container">
    <p class="title is-4">${projectName}</p>
    <form action="${contextPath}/select" method="post" enctype="multipart/form-data">

        <div class="field">
          <label class="label">Target Attribute</label>
          <div class="control">
            <input class="input" type="text" name="targetAttribute" placeholder="Target Attribute" value="${TARGET_ATTRIBUTE}">
          </div>
        </div>

        <div class="field">
          <label class="label">Target Value</label>
          <div class="control">
            <input class="input" type="text" name="targetValue" placeholder="Target Value" value="${TARGET_VALUE}">
          </div>
        </div>

        <div class="field">
          <label class="label">Ignore Attributes</label>
          <div class="control">
            <input class="input" type="text" name="ignoredAttributes" placeholder="Ignored Attributes" value="${IGNORED_ATTRIBUTES}">
          </div>
        </div>

        <div class="field">
          <label class="label">Suspected Dependencies</label>
          <div class="control">
            <input class="input" type="text" name="suspectedDependencies" placeholder="Suspected Dependencies" value="${SUSPECTED_DEPENDENCIES}">
          </div>
        </div>

        <div class="field">
          <div class="control">
              <label class="checkbox">
                  <input type="checkbox" name="preprocessing" value="numeric_to_nominal" ${NUMERIC_TO_NOMINAL_CHECKED}>
                  Convert Numeric to Nominal
            </label>
              <label class="checkbox">
                  <input type="checkbox" name="preprocessing" value="replace_missing" ${REPLACE_MISSING_CHECKED}>
                  Replace Missing Nominal Values with Constant
            </label>
          </div>
        </div>

        <div class="field">
          <div class="control">
            <input type="submit" value="Submit" class="button is-primary">
          </div>
        </div>
    </form>

    <form action="${contextPath}/" method="get">
        <div class="field">
          <div class="control">
            <input type="submit" value="Reset" class="button is-light">
          </div>
        </div>
    </form>
</div>
</section>
