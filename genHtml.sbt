val genHtmlLocal = TaskKey[Unit]("genHtmlLocal")

genHtmlLocal := {
  val js = "./js/target/scala-2.12/scalajspack-fastopt.js"
  val html = gen(js)
  IO.write(file("index.html"), html)
}

TaskKey[Unit]("genAndCheckHtml") := {
  genHtmlLocal.value
  val diff = sys.process.Process("git diff").!!
  if(diff.nonEmpty){
    sys.error("Working directory is dirty!\n" + diff)
  }
}

TaskKey[Unit]("genHtmlPublish") := {
  val js = "./scalajspack.js"
  val html = gen(js)
  IO.write(file("index.html"), html)
}

val codeMirrorURL = "https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.22.2/"

def gen(js: String) = s"""<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>scalajspack - JSON to MessagePack converter powered by scala-js</title>
    <script type="text/javascript" src="${js}"></script>
    <script type="text/javascript" src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
    <link rel="stylesheet" href="${codeMirrorURL}/codemirror.css">
    <script src="${codeMirrorURL}/codemirror.js"></script>
    <script src="${codeMirrorURL}/mode/javascript/javascript.js"></script>
  </head>
  <body>
    <p><a target="_brank" href="https://github.com/xuwei-k/scalajspack">https://github.com/xuwei-k/scalajspack</a></p>
    <div>
      <textarea id="input_js" style="height: 200px; width: 400px;">{"aaa": [true, 1000, null]}</textarea>
      <button id="convert_button">convert</button>
    </div>
    <div>
      <pre id="output_msgpack"></pre>
      <pre id="error" style="color: red;"></pre>
    </div>
  </body>

<script type="text/javascript">
$$(function(){
  var cm = CodeMirror.fromTextArea(document.getElementById("input_js"), {
    lineNumbers: true,
    mode: "javascript"
  });

  var run = function(){
    try{
      var r = scalajspack.Main.convert(cm.getValue());
      $$("#output_msgpack").text(r);
      $$("#error").text("");
    }catch(e){
      $$("#error").text(e);
      $$("#output_msgpack").text("");
    }
  };

  cm.on("change", run);

  $$("#convert_button").click(function(){
    run();
  });

  $$(document).ready(function(){
    run();
  });
});
</script>

</html>
"""
