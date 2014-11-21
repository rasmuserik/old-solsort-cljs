(defproject solsort-server "0.1.0-SNAPSHOT"
  :description "solsort.com server"
  :url "http://solsort.com"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.319.0-6b1aca-alpha"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [
             {:id "server"
              :source-paths ["src"]
              :compiler {
                :output-to "build/server.js"
                :output-dir "build"
                :optimizations :advanced
                :externs ["externs/express.js"
                          "node_modules/nodejs-externs/externs/assert.js" "node_modules/nodejs-externs/externs/buffer.js" "node_modules/nodejs-externs/externs/child_process.js" "node_modules/nodejs-externs/externs/cluster.js" "node_modules/nodejs-externs/externs/core.js" "node_modules/nodejs-externs/externs/crypto.js" "node_modules/nodejs-externs/externs/dgram.js" "node_modules/nodejs-externs/externs/dns.js" "node_modules/nodejs-externs/externs/domain.js" "node_modules/nodejs-externs/externs/events.js" "node_modules/nodejs-externs/externs/fs.js" "node_modules/nodejs-externs/externs/http.js" "node_modules/nodejs-externs/externs/https.js" "node_modules/nodejs-externs/externs/net.js" "node_modules/nodejs-externs/externs/os.js" "node_modules/nodejs-externs/externs/path.js" "node_modules/nodejs-externs/externs/process.js" "node_modules/nodejs-externs/externs/punycode.js" "node_modules/nodejs-externs/externs/querystring.js" "node_modules/nodejs-externs/externs/readline.js" "node_modules/nodejs-externs/externs/repl.js" "node_modules/nodejs-externs/externs/stream.js" "node_modules/nodejs-externs/externs/string_decoder.js" "node_modules/nodejs-externs/externs/tls.js" "node_modules/nodejs-externs/externs/tty.js" "node_modules/nodejs-externs/externs/url.js" "node_modules/nodejs-externs/externs/util.js" "node_modules/nodejs-externs/externs/vm.js" "node_modules/nodejs-externs/externs/zlib.js"]

                :pretty-print true
                :source-map "build/server.map"}}]})
