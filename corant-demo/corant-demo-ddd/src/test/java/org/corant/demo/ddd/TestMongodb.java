/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.demo.ddd;

import javax.inject.Inject;
import javax.inject.Named;
import org.bson.Document;
import org.corant.devops.test.unit.CorantJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * corant-demo-ddd
 *
 * @author bingo 下午8:52:43
 *
 */
@RunWith(CorantJUnit4ClassRunner.class)
public class TestMongodb {

  @Inject
  @Named("182.GADB-APSCM")
  MongoDatabase db;

  @Test
  public void test() {
    MongoCollection<Document> coll = db.getCollection("articlePublishInfo");
    for (Document doc : coll.find()) {
      System.out.println(doc.get("_id"));
    }
  }
}