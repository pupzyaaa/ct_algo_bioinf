# FASTQ Parser

В коде оставлены комментарии, поясняющие действия. Обработка парсинга свелась к массивам длин прочтений и подсчета GC и Phred сумм. Во время парсинга также сразу производился кастомный тримминг. Оттуда уже взяли запрашиваемые данные и сравнили их с массивом прочтений, подвергшихся триммингу. 

Кроме того, провели тримминг шириной скользящего окна 5 и качеством 30 с помощью Trimmomatic запустив из командной строки:
```
fastq % java -jar trimmomatic-0.40.jar SE reads.fastq.txt trimmed.fastq SLIDINGWINDOW:5:30
```
Получили такой результат:
```
TrimmomaticSE: Started with arguments:
reads.fastq.txt trimmed.fastq SLIDINGWINDOW:5:30
Automatically using 1 threads
Quality encoding detected as phred33
Input Reads: 239366 Surviving: 224338 (93,72%) Dropped: 15028 (6,28%)
TrimmomaticSE: Completed successfully
```
Подробнее вывод Trimmomatic обработали с помощью TrimmomaticProcessor.

Все полученные результаты вывели в Main.java.
