import { Html, Head, Main, NextScript } from 'next/document';
// 공통적으로 적용할 HTML 마크업 중심
// 꼭 <Html>, <Head>, <Main>, <NextScript> 요소를 리턴해줘야 함
// 언제나 서버에서 실행되므로 브라우저 api 또는 이벤트 핸들러가 포함된 코드는 실행되지 않음

export default function Document() {
  return (
    <Html>
      <Head>
        <meta name='description' content='Generated by create next app' />
        <link rel='icon' href='/favicon.ico' />
      </Head>
      <body>
        <Main />
        <NextScript />
      </body>
    </Html>
  );
}
