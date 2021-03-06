<#if directDependencies??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Direct Dependencies</p>
            <div><pre><code>
${directDependencies}
            </code></pre>
            </div>
        </div>
    </section>
</#if>

<#if correlatedAttributes??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Highly Correlated Attributes</p>
            <div><pre><code>
${correlatedAttributes}
        </code></pre>
            </div>
        </div>
    </section>
</#if>

<#if selectedAttributes??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Selected Attributes</p>
            <div><pre><code>
${selectedAttributes}
        </code></pre>
            </div>
        </div>
    </section>
</#if>

<hr class="hr">

<#if tree??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Tree</p>
            ${tree}
        </div>
    </section>
</#if>

<#if assumptionStumps??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Correlation Assumptions Tree</p>
            ${assumptionStumps}
        </div>
    </section>
</#if>

<hr class="hr">

<#if rules??>
    <section class="section">
        <div class="container">
            <p class="title is-4">Rules</p>
            <div><pre><code>
${rules}
            </code></pre>
            </div>
        </div>
    </section>
</#if>

<#if evaluation??>
<hr class="hr">

    <section class="section">
        <div class="container">
            <p class="title is-4">Evaluation</p>
            <div><pre><code>
${evaluation}
            </code></pre>
            </div>
        </div>
</section>
</#if>