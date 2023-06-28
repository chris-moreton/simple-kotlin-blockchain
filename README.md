## A Simple Kotlin Blockchain

Just a little project to help me to understand blockchain implementation a little better. I wanted to get a real understanding of how nodes reach consensus. There is no cryptography going on here, just a simple proof-of-work algorithm that handles
block clashes by maintaining forks and choosing the longest chain.

Now that it is working, I will have to see if I can maintain my interest and tidy it up a bit.

### How to run

```bash
    ./gradlew run
```

It simulates a network with hard-coded parameters for number of nodes, network difficulty, min and max transactions per block and other tweakables.

Then it throws out a bunch of logs to the console, periodically printing out the blockchain state of each node.

Something like this...

```
2023-06-28T21:28:43.130507: Node Node10 mined a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700. Index is 71, previous hash is 000050e23194c5482be0b908b5d56866c5f27017765f6f9e6b378d3fa4f871eb
2023-06-28T21:28:43.130910: Newly-mined block is valid, node Node10 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to chain tip 000050e23194c5482be0b908b5d56866c5f27017765f6f9e6b378d3fa4f871eb!
2023-06-28T21:28:43.131151: Node Node19 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.131296: Node Node19 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.131402: Node Node3 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.131542: Node Node3 is cancelling mining job because a valid block was received from a peer.
2023-06-28T21:28:43.131605: Node Node3 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.131727: Node Node8 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.131779: Node Node8 is cancelling mining job because a valid block was received from a peer.
2023-06-28T21:28:43.131784: Node Node8 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.131791: Node Node8 stopped mining because the mineLock was lifted.
2023-06-28T21:28:43.131631: Node Node3 stopped mining because the mineLock was lifted.
2023-06-28T21:28:43.131800: Node Node20 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.136955: Node Node20 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.137019: Node Node7 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.137101: Node Node7 is cancelling mining job because a valid block was received from a peer.
2023-06-28T21:28:43.137113: Node Node7 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.137122: Node Node7 stopped mining because the mineLock was lifted.
2023-06-28T21:28:43.137145: Node Node2 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.137227: Node Node2 is cancelling mining job because a valid block was received from a peer.
2023-06-28T21:28:43.137239: Node Node2 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.137262: Node Node2 stopped mining because the mineLock was lifted.
2023-06-28T21:28:43.137276: Node Node17 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.137287: Node Node17 received a block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 that doesn't fit on the main chain. Block previous hash is 000050e23194c5482be0b908b5d56866c5f27017765f6f9e6b378d3fa4f871eb. Chain tip is 00002981cb980d598bdf8d739e6574a02c5db068a9083cf2fc785c0b4ce28662.
2023-06-28T21:28:43.137296: Node Node17 has 2 forks.
2023-06-28T21:28:43.137856: Node Node17 added block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to an existing fork [1]
2023-06-28T21:28:43.137958: Node Node17 is checking if fork 1 is longer than the main chain.
2023-06-28T21:28:43.137974: Node Node17 has a fork [1] that is longer than the main chain. Swapping to the fork.
2023-06-28T21:28:43.138062: Node Node14 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.138146: Node Node14 added a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to the chain!
2023-06-28T21:28:43.138157: Node Node4 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.138165: Node Node4 received a block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 that doesn't fit on the main chain. Block previous hash is 000050e23194c5482be0b908b5d56866c5f27017765f6f9e6b378d3fa4f871eb. Chain tip is 00002981cb980d598bdf8d739e6574a02c5db068a9083cf2fc785c0b4ce28662.
2023-06-28T21:28:43.138174: Node Node4 has 2 forks.
2023-06-28T21:28:43.138254: Node Node4 added block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 to an existing fork [1]
2023-06-28T21:28:43.138267: Node Node4 is checking if fork 1 is longer than the main chain.
2023-06-28T21:28:43.138276: Node Node4 has a fork [1] that is longer than the main chain. Swapping to the fork.
2023-06-28T21:28:43.138326: Node Node18 received a new block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 from a peer. Block has index 71. Last chain index is 70. Chain size is 71.
2023-06-28T21:28:43.138337: Node Node18 received a block 00001b332f891bee72bd69bed11b8ed54fe8961488179f050e45961e2614f700 that doesn't fit on the main chain. Block previous hash is 000050e23194c5482be0b908b5d56866c5f27017765f6f9e6b378d3fa4f871eb. Chain tip is 00002981cb980d598bdf8d739e6574a02c5db068a9083cf2fc785c0b4ce28662.
2023-06-28T21:28:43.138345: Node Node18 has 2 forks.

2023-06-28T21:28:44.503965: Node1: 74 ESIS 2546 a855 68e2 67d5 b66e 8e71 72e5 1249 240b dd35 ac2a 516a ae11 1e53 ad28 0505 c2f3 12b9 c9f0 2733 ab11 209f 3ae6 5780 6893 da6a 803f d70c a7d3 9f91 8054 bf57 2d64 0c5f 7f6d b6a5 4e98 c44f a117 80b0 fa78 ccfb 8298 4e32 eab8 a897 5a06 3a1c e382 8914 5b0f dc63 e2f3 22d4 ce1c 8395 3336 2cf8 c1d1 c950 467d f2d2 ce42 dab7 8be9 cc4e 05cc d8b2 c0f4 71eb f700 40fb b7dd 
2023-06-28T21:28:44.504061: Node2: 74 ESIS 2546 a855 68e2 67d5 b66e 8e71 72e5 1249 240b dd35 ac2a 516a ae11 1e53 ad28 0505 c2f3 12b9 c9f0 2733 ab11 209f 3ae6 5780 6893 da6a 803f d70c a7d3 9f91 8054 bf57 2d64 0c5f 7f6d b6a5 4e98 c44f a117 80b0 fa78 ccfb 8298 4e32 eab8 a897 5a06 3a1c e382 8914 5b0f dc63 e2f3 22d4 ce1c 8395 3336 2cf8 c1d1 c950 467d f2d2 ce42 dab7 8be9 cc4e 05cc d8b2 c0f4 71eb f700 40fb b7dd 
2023-06-28T21:28:44.504168: Node3: 74 ESIS 2546 a855 68e2 67d5 b66e 8e71 72e5 1249 240b dd35 ac2a 516a ae11 1e53 ad28 0505 c2f3 12b9 c9f0 2733 ab11 209f 3ae6 5780 6893 da6a 803f d70c a7d3 9f91 8054 bf57 2d64 0c5f 7f6d b6a5 4e98 c44f a117 80b0 fa78 ccfb 8298 4e32 eab8 a897 5a06 3a1c e382 8914 5b0f dc63 e2f3 22d4 ce1c 8395 3336 2cf8 c1d1 c950 467d f2d2 ce42 dab7 8be9 cc4e 05cc d8b2 c0f4 71eb f700 40fb b7dd 
2023-06-28T21:28:44.504231: Node4: 74 ESIS 2546 a855 68e2 67d5 b66e 8e71 72e5 1249 240b dd35 ac2a 516a ae11 1e53 ad28 0505 c2f3 12b9 c9f0 2733 ab11 209f 3ae6 5780 6893 da6a 803f d70c a7d3 9f91 8054 bf57 2d64 0c5f 7f6d b6a5 4e98 c44f a117 80b0 fa78 ccfb 8298 4e32 eab8 a897 5a06 3a1c e382 8914 5b0f dc63 e2f3 22d4 ce1c 8395 3336 2cf8 c1d1 c950 467d f2d2 ce42 dab7 8be9 cc4e 05cc d8b2 c0f4 71eb f700 40fb b7dd 
2023-06-28T21:28:44.504292: Node5: 74 ESIS 2546 a855 68e2 67d5 b66e 8e71 72e5 1249 240b dd35 ac2a 516a ae11 1e53 ad28 0505 c2f3 12b9 c9f0 2733 ab11 209f 3ae6 5780 6893 da6a 803f d70c a7d3 9f91 8054 bf57 2d64 0c5f 7f6d b6a5 4e98 c44f a117 80b0 fa78 ccfb 8298 4e32 eab8 a897 5a06 3a1c e382 8914 5b0f dc63 e2f3 22d4 ce1c 8395 3336 2cf8 c1d1 c950 467d f2d2 ce42 dab7 8be9 cc4e 05cc d8b2 c0f4 71eb f700 40fb b7dd 

2023-06-28T21:28:44.507422: Total score: 35816, max score: 35816
2023-06-28T21:28:44.507516: Same until block: 74 / 74
2023-06-28T21:28:44.507549: Similarity: 100

```