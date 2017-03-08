(ns takelist.route)

(def routes
  ["/" {"" :home
        "order" :order
        "order-confirmation" :order-confirmation
        "post-order" :post-order
        "oauth2-code" :oauth2-code}])