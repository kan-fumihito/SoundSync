#include <avr/io.h>
#include <avr/interrupt.h>

#define FOSC 8000000
#define BAUD 9600
#define MYUBRR (FOSC / 16 / BAUD) - 1

int pcint;
unsigned char pc = 0, pb = 0, pd = 0;

void USART_Init(unsigned int baud)
{
    UBRR1H = (unsigned char)(baud >> 8);
    UBRR1L = (unsigned char)(0xFF & baud);

    UCSR1B = 0b00001000;
    UCSR1C = 0b00000110;
}

void USART_Transmit(unsigned char data)
{
    while (!(UCSR1A & (1 << UDRE1)));
    UDR1 = data;
}

void interrupt_init(void);
void interrupt_mask(void);
void tc0_init(void);
void tc0_start(void);
ISR(PCINT0_vect);
ISR(PCINT1_vect);
ISR(PCINT2_vect);
ISR(TIMER0_OVF_vect);

int main(void)
{
    PORTB = 0x3F;
    DDRB = 0x00;
    PORTC = 0x01;
    DDRC = 0x00;
    PORTD = 0xFC;
    DDRD = 0x02;

    USART_Init(MYUBRR);
    interrupt_init();
    sei();

    while (1);
}

void interrupt_init(void)
{
    PCICR = 0x07;
    PCMSK0 = 0x3F;
    PCMSK1 = 0x01;
    PCMSK2 = 0xFC;
}

void interrupt_mask(void)
{
    PCICR = 0x00;
    PCMSK0 = 0x00;
    PCMSK1 = 0x00;
    PCMSK2 = 0x00;
}

void tc0_init(void)
{
    TCCR0 = 0x00; //タイマ/カウンタ0標準動作，OC0切断，停止
    TIMSK = 0x00; //タイマ/カウンタ0割込みマスク
}

void tc0_start(void)
{
    CNT0 = 0x00;  //タイマ/カウンタ0クリア
    TCCR0 = 0x06; //タイマ/カウンタ0標準動作，OC0切断，256分周
    TIMSK = 0x01; //タイマ/カウンタ0オーバーフロー割込みイネーブル
}

ISR(PCINT0_vect)
{
    interrupt_mask();
    pcint = 0;
    tc0_start();
}

ISR(PCINT1_vect)
{
    interrupt_mask();
    pcint = 1;
    tc0_start();
}

ISR(PCINT2_vect)
{
    interrupt_mask();
    pcint = 2;
    tc0_start();
}

ISR(TIMER0_OVF_vect)
{
    unsigned char sw, count = 0;
    TCCR0 = 0x00;
    TIMSK = 0x00;

    switch (pcint)
    {
    case 0:
        if (PINC & 0x01)
            USART_Transmit('m');
        break;

    case 1:
        sw = PINB & 0x3F;
        if (pb != sw)
        {
            pb = (pb ^ sw) & sw;
            do
            {
                if (pb == 0x01)
                    USART_Transmit('g' + count);
                count++;
            } while (pb >> 1);
        }
        pb = sw;
        break;

    case 2:
        sw = PIND & 0xFC;
        if (pd != sw)
        {
            pd = (pd ^ sw) & sw;
            do
            {
                if (pd == 0x01)
                    USART_Transmit('a' + count);
                count++;
            } while (pd >> 1);
        }
        pd = sw;
        break;
    }

    PCIFR |= 0x07;
    interrupt_init();
}
