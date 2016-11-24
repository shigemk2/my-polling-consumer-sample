package com.example

import akka.actor._

object PollingConsumerDriver extends CompletableApp(1) {
}

case class WorkNeeded()
case class WorkOnItem(workItem: WorkItem)

class WorkConsumer(workItemsProvider: ActorRef) extends Actor {
  var totalItemsWorkedOn = 0

  def performWorkOn(workItem: WorkItem) = {
    totalItemsWorkedOn = totalItemsWorkedOn + 1
    if (totalItemsWorkedOn >= 15) {
      context.stop(self)
      PollingConsumerDriver.completeAll
    }
  }

  override def postStop() = {
    context.stop(workItemsProvider)
  }

  def receive = {
    case allocated: WorkItemsAllocated =>
      println("WorkItemsAllocated...")
      allocated.workItems map { workItem =>
        self ! WorkOnItem(workItem)
      }
      self ! WorkNeeded()
    case workNeeded: WorkNeeded =>
      println("WorkNeeded...")
      workItemsProvider ! AllocateWorkItems(5)
    case workOnItem: WorkOnItem =>
      println(s"Performed work on: ${workOnItem.workItem.name}")
      performWorkOn(workOnItem.workItem)
  }
}

case class AllocateWorkItems(numberOfItems: Int)
case class WorkItemsAllocated(workItems: List[WorkItem])
case class WorkItem(name: String)

class WorkItemsProvider extends Actor {
  var workItemsNamed: Int = 0

  def allocateWorkItems(numberOfItems: Int): List[WorkItem] = {
    var allocatedWorkItems = List[WorkItem]()
    for (itemCount <- 1 to numberOfItems) {
      val nameIndex = workItemsNamed + itemCount
      allocatedWorkItems = allocatedWorkItems :+ WorkItem("WorkItem" + nameIndex)
    }
    workItemsNamed = workItemsNamed + numberOfItems
    allocatedWorkItems
  }

  def receive = {
    case request: AllocateWorkItems =>
      sender ! WorkItemsAllocated(allocateWorkItems(request.numberOfItems))
  }
}
