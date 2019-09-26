<datalist id="preprocessingOptions">
<option value="nominal_numeric">Nominal + Numeric</option>
<option value="binary_numeric">Binary + Numeric</option>
<option value="all_nominal">All Nominal</option>
<option value="all_binary">All Binary</option>
</datalist>

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
            <label class="radio">
              <input type="radio" name="preprocessing" value="nominal_numeric">
              Nominal + Numeric
            </label>
            <label class="radio">
              <input type="radio" name="preprocessing" value="binary_numeric">
              Binary + Numeric
            </label>
            <label class="radio">
              <input type="radio" name="preprocessing" value="all_nominal">
              All Nominal
            </label>
            <label class="radio">
              <input type="radio" name="preprocessing" value="all_binary">
              All Binary
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
