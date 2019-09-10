<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">

<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>Results</title>
</head>

<body>
    <div>
        <h3>{{TARGET_VARIABLE}}</h3>
    </div>
    <div>
        <h3>Tree</h3>
        <img src="{{FOLDER}}tree.png" />
    </div>
    <div>
        <h3>Rules</h3>
        <div><pre><code>
{{RULES}}
        </code></pre></div>
    </div>
    <div>
        <h3>Evaluation</h3>
        <div><pre><code>
{{EVALUATION}}
        </code></pre></div>
    </div>
    <div>
        <h3>Common Values</h3>
        <div><pre><code>
{{COMMON_VALUES}}
        </code></pre></div>
    </div>
    <div>
        <h3>Clusters</h3>
        <div><pre><code>
{{CLUSTERS}}
        </code></pre></div>
    </div>
    <div>
        <h3>Direct Dependencies</h3>
        <div><pre><code>
{{DIRECT_DEPENDENCIES}}
        </code></pre></div>
    </div>
</body>

</html>