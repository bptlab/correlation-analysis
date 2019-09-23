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
            <p class="subtitle">Selected Attributes</p>
            <div><pre><code>
{{SELECTED_ATTRIBUTES}}
            </code></pre></div>
        </div>
    </section>


    <section class="section">
        <div class="container">
            <p class="subtitle">Tree</p>
            <img src="{{FOLDER}}tree.png" />
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="subtitle">Rules</p>
            <div><pre><code>
{{RULES}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="subtitle">Evaluation</p>
            <div><pre><code>
{{EVALUATION}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="subtitle">Common values between cluster centroids</p>
            <div><pre><code>
{{CLUSTERS}}
            </code></pre></div>
        </div>
    </section>

    <section class="section">
        <div class="container">
            <p class="subtitle">Direct Dependencies</p>
            <div><pre><code>
{{DIRECT_DEPENDENCIES}}
            </code></pre></div>
        </div>
    </section>

  </body>
</html>
