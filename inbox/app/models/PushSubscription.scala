package models

import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.Future
import awswrappers.dynamodb._
import scala.concurrent.ExecutionContext.Implicits.global

object PushSubscription {
  val TableName = "inbox-push-subscriptions"

  def subscribe(endpoint: String, userId: String, topic: String) = {
    client.updateItemFuture(new UpdateItemRequest()
      .withTableName(TableName)
      .withKey(Map("topic" -> new AttributeValue().withS(topic)))
      .withAttributeUpdates(Map[String, AttributeValueUpdate](
      "subscriptions" -> new AttributeValueUpdate()
        .withAction(AttributeAction.ADD)
        .withValue(new AttributeValue().withS(userId))
    ))
    )
  }

  def unsubscribe(endpoint: String, userId: String, topic: String) = {
    client.updateItemFuture(new UpdateItemRequest()
      .withTableName(TableName)
      .withKey(Map("topic" -> new AttributeValue().withS(topic)))
      .withAttributeUpdates(Map[String, AttributeValueUpdate](
      "subscriptions" -> new AttributeValueUpdate()
        .withAction(AttributeAction.DELETE)
        .withValue(new AttributeValue().withS(userId))
    ))
    )
  }

  def getSubscriptions(topic: String): Future[Seq[String]] = {
    client.getItemFuture(new GetItemRequest()
      .withTableName(TableName)
      .withKey(Map("topic" -> new AttributeValue().withS(topic)))) map { response =>
      Option(response.getItem) map { item =>
        item.asScala.get("subscriptions").get.getSS.toSeq
      } getOrElse Seq.empty
    }
  }
}