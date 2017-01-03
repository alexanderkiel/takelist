(ns takelist.db-test
  (:require [clojure.test :refer :all]
            [takelist.db :as db :refer :all]))

(deftest find-user-query-test
  (are [props constraints query] (= query (#'db/find-user-query props constraints))
    [:id]
    {:id 1}
    "select id from tkl_user where id = ?"

    [:id :name]
    {:id 1}
    "select id, name from tkl_user where id = ?"

    [:id]
    {:issuer "foo", :subject "bar"}
    "select id from tkl_user where issuer = ? and subject = ?"))
