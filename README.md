PROJECT DESCRIPTION:  

Twitch is a live streaming platform where streamers broadcast live video content, and users interact with them through live chat.  

-> Performed sentiment analysis on live stream chat messages from multiple Twitch channels using CoreNLP(Stanford NLP) to categorize sentiment as positive, negative, or neutral on a 0 - 4 rating scale.  

-> Transformed the data to JSON and utilized Kafka as data sink and stored the data in S3 for further analysis.


 USED TECHNOLOGIES, LIBRARIES AND APIs:
 1. Twitch API       -  To retrieve live stream data
 2. Apache Flink     -  To process the live stream data
 3. Core NLP Library -  To analyze the sentiment of live stream messgages
 4. Google Gson      -  To Convert the data to Json
 5. Apache Kafka     -  Utilized Kafka as data sink
 6. AWS S3           -  Stored the Json data in s3 for further analysis
    
 PROJECT ARCHITECTURE:  
 
![image](https://github.com/Swathi-Reddy1408/live-stream-chat-sentiment-analysis/assets/52827609/25c7b745-c9d2-449b-abb9-425601739248)  
