## Overview

A trek is a record of the user's journey along a path. It has these properties:

### val intentId: IntentId
Each trek starts with an intent. The intent can invoke a trek only once and be completed, or it can be scheduled or set to repeat. 

### val superId: TrekId?
Treks are a record of a journey along a path, but each step can contain its own steps, and any of those steps can contain steps to support paths that are arbitrarily complex. When a user expands the path of a trek to these substeps, a new trek is created to keep the record of that path. The subtrek keeps a reference to the supertrek with superId. Treks with a null superId are "root treks"

### val pathStepId: PathStepId?
A subtrek must keep a reference to the pathStep of its root step within the supertrek's path. 

### val rootId: StepId
This is the step or path where the trek starts. If it contains steps, those steps are part of the trek. If it has no pathsteps, this single step encompasses the entire trek.