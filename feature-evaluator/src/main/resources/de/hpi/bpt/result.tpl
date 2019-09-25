<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>{{PROJECT}}</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bulma/0.7.5/css/bulma.min.css">
    <script defer src="https://use.fontawesome.com/releases/v5.3.1/js/all.js"></script>
  </head>
  <body>

    <section class="section">
        <div class="container">
            <h1 class="title">{{TARGET_VARIABLE}}</h1>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Direct Dependencies</p>
            <div><pre><code>
{{DIRECT_DEPENDENCIES}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Highly Correlated Attributes</p>
            <div><pre><code>
{{CORRELATED_ATTRIBUTES}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Selected Attributes</p>
            <div><pre><code>
{{SELECTED_ATTRIBUTES}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Correlation Assumptions</p>
            {{ASSUMPTION_TREES}}
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Tree</p>
            <img src="{{FOLDER}}tree.png" />
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Rules</p>
            <div><pre><code>
{{RULES}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Evaluation</p>
            <div><pre><code>
{{EVALUATION}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Top 3 Common Values Between Instances</p>
            <div><pre><code>
{{COMMON_VALUES}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="title is-4">Cluster Centroids</p>
            <div><pre><code>
{{CLUSTERS}}
            </code></pre></div>
        </div>
    </section>

  </body>
</html>
