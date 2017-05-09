(ns takelist.util-test
  (:require [clojure.spec :as s]
            [clojure.spec.test :as st]
            [clojure.test :refer :all]
            [clojure.test.check]
            [takelist.util :refer :all]
            [juxt.iota :refer [given]]))

(st/instrument)

(deftest error-resp-test
  (given (error-resp 400 "foo")
    :status := 400
    :body "foo")

  (is (not (some :failure (st/check `error-resp)))))
