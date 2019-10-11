<#if DIRECT_DEPENDENCIES??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Direct Dependencies</p>
            <div><pre><code>
${DIRECT_DEPENDENCIES}
            </code></pre></div>
        </div>
    </section>
</#if>

<#if CORRELATED_ATTRIBUTES??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Highly Correlated Attributes</p>
            <div><pre><code>
${CORRELATED_ATTRIBUTES}
            </code></pre></div>
        </div>
    </section>
</#if>

<#if SELECTED_ATTRIBUTES??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Selected Attributes</p>
            <div><pre><code>
${SELECTED_ATTRIBUTES}
            </code></pre></div>
        </div>
    </section>
</#if>

<#if assumptionStumps??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Correlation Assumptions</p>
            ${assumptionStumps}
        </div>
    </section>
</#if>

<#if TREE??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Tree</p>
            ${TREE}
        </div>
    </section>
</#if>

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