# Kotlin Conf 2017 - Coroutine Part 정리

Continuation 은 Generice Callback Interface 이다. Continuation 은 단순히 Callback 을 Fancy 하게 부르는 것이라고 함.

<img width="1020" alt="image" src="https://user-images.githubusercontent.com/57784077/182021120-2551ae22-e1ac-4058-b50f-d5a22e9df7f5.png">

결코 Magic 은 존재하지 않고, Coroutine 이 Suspension 을 사용할 수 있는 이유는 Suspend point 를 LABEL 로 정의하기 때문임.

<img width="814" alt="image" src="https://user-images.githubusercontent.com/57784077/182021208-0b073885-df60-418a-aa73-7660207a1314.png">

쉽게 우리가 이를 코드로 작성한다면 Switch 로 작성할 수 있는데 그렇게 된다면 코드는 아래와 같은 코드가 될것임.

<img width="861" alt="image" src="https://user-images.githubusercontent.com/57784077/182021222-6ba581fe-0a5e-461e-9745-3b65eaed1842.png">

Suspending 된 Coroutine 안에는 현재 label 을 적재할 Store 가 하나 존재한다. 이것이 Continuation 임. 우리는 각 Label 이 붙은 함수들간에 Continuation 을 주고 받아야 함. 따라서 우리는 Continuation 을 State Machine 으로 이용할 것 임

<img width="823" alt="image" src="https://user-images.githubusercontent.com/57784077/182021311-27b03351-086e-46ad-ab43-ccf8e90c32ef.png">

이제 Store 에 한가지씩 적재하기 시작함.

<img width="825" alt="image" src="https://user-images.githubusercontent.com/57784077/182021363-d97ccd38-1c0e-43fe-a419-c29e9fe056cd.png">

근데 우리가 이렇게 Label 을 통하여 분배했을때, 다음 Label 로 넘어가기 위한 방법이 필요하다. 근데 위에서 말했듯이 Coroutine 은 CallBack 이다. 따라서 resume function 을 통해 완료 되면 다음 Label 로 넘어갈 수 있게 설계할 것이다.

<img width="898" alt="image" src="https://user-images.githubusercontent.com/57784077/182021505-9fea3673-8cf8-4880-bb95-50a086857f06.png">

위의 코드를 보면 단순히 resume 을 통하여 stateMachine 만 update 를 하고 코드를 다시 실행시키는 방식이다. 하지만 lable 은 1이 올라가 있는 상태이므로, 다음 suspend function 을 실행시킬 수 있다. 왜 Suspend function 에서 label 을 나누는 기준이 중요한지를 이렇게 보니 좀 알 것 같다.

<img width="893" alt="image" src="https://user-images.githubusercontent.com/57784077/182021599-445f11b9-cb05-406a-970c-cabd79908f3d.png">

위와 같이 Check 하는 이유는 내 자신의 State Machine 이 아닌, 다른 State Machine 이 들어오는 것을 방지하기 위해 체크를 하게 됨. 결국 다시 들어와서 case 1 을 타게 되고, 결국 suspend 를 통한 switching 은 위와 같은 과정의 반복임.

어떻게 Coroutine 이 suspend 

<img width="1165" alt="image" src="https://user-images.githubusercontent.com/57784077/182022890-2d5ff59f-d09e-46c6-91ea-c93ef5daec5a.png">

기존의 Java Feature 를 Coroutine 으로 Integration 하는 방법

<img width="1232" alt="image" src="https://user-images.githubusercontent.com/57784077/182023485-df4b105d-ffcc-4d6f-b453-291d5764c5b6.png">

## CSP

Communication Sequential Processes  