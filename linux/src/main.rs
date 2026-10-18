use iced::widget::{button, center, column, text};
use iced::{Center, Element};

fn main() -> iced::Result {
    iced::application(Counter::default, Counter::update, Counter::view)
        .title("AmlethMP")
        .run()
}

#[derive(Default)]
struct Counter {
    value: i64,
}

#[derive(Debug, Clone, Copy)]
enum Message {
    Increment,
    Decrement,
}

impl Counter {
    fn update(&mut self, message: Message) {
        match message {
            Message::Increment => self.value += 1,
            Message::Decrement => self.value -= 1,
        }
    }

    fn view(&self) -> Element<'_, Message> {
        center(
            column![
                button("+").on_press(Message::Increment),
                text(self.value).size(40),
                button("-").on_press(Message::Decrement),
            ]
            .spacing(10)
            .align_x(Center),
        )
        .into()
    }
}
