<section class="section">
    <div class="container">
        <p class="title is-4">${projectName}</p>
        <p class="title is-4">Target Attribute: ${TARGET_ATTRIBUTE}</p>

        <form action="${contextPath}/ignore" method="post" enctype="multipart/form-data">

            <div class="field">
                <label class="label">Ignore Attributes</label>
                <div class="box" style="max-height:15em; overflow:auto">
                    <div class="control">
                        <#list attributes as attribute>
                            <label class="checkbox">
                                <input type="checkbox" name="ignoredAttributes" value="${attribute}">
                                ${attribute}
                            </label>
                            <br/>
                        </#list>
                    </div>
                </div>
            </div>

            <div class="field">
                <div class="control">
                    <input type="submit" value="Submit" class="button is-primary">
                </div>
            </div>
        </form>

        <form action="${contextPath}/" method="post">
            <div class="field">
                <div class="control">
                    <input type="submit" value="Reset" class="button is-light">
                </div>
            </div>
        </form>

        <section class="section">
            <div class="container">
                <p class="title is-4">Ignored Attributes</p>
                <div><pre><code>
${IGNORED_ATTRIBUTES}
            </code></pre>
                </div>
            </div>
        </section>

        <section class="section">
            <div class="container">
                <p class="title is-4">Suspected Dependencies</p>
                <div><pre><code>
${SUSPECTED_DEPENDENCIES}
            </code></pre>
                </div>
            </div>
        </section>
    </div>
</section>
