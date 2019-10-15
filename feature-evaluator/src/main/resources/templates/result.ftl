<section class="section">
    <#if DIRECT_DEPENDENCIES??>
        <div class="container">
            <p class="title is-4">Direct Dependencies</p>
            <div><pre><code>
${DIRECT_DEPENDENCIES}
            </code></pre></div>
        </div>
    </#if>

    <#if CORRELATED_ATTRIBUTES??>
        <div class="container">
            <p class="title is-4">Highly Correlated Attributes</p>
            <div><pre><code>
${CORRELATED_ATTRIBUTES}
        </code></pre>
            </div>
        </div>
    </#if>

    <#if SELECTED_ATTRIBUTES??>
        <div class="container">
            <p class="title is-4">Selected Attributes</p>
            <div><pre><code>
${SELECTED_ATTRIBUTES}
        </code></pre>
            </div>
        </div>
    </#if>
</section>


<section class="section">
    <#if assumptionStumps??>
        <div class="container">
            <p class="title is-4">Correlation Assumptions</p>
            ${assumptionStumps}
        </div>
    </#if>

    <#if TREE??>
        <div class="container">
            <p class="title is-4">Tree</p>
            ${TREE}
        </div>
    </#if>
</section>


<#if RULES??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Rules</p>
            <div><pre><code>
${RULES}
            </code></pre></div>
        </div>
    </section>
</#if>

<#if EVALUATION??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Evaluation</p>
            <div><pre><code>
${EVALUATION}
            </code></pre></div>
        </div>
    </section>
</#if>